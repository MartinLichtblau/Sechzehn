'use strict'

const Venue = use('App/Model/Venue')
const Database = use('Database')
const Validator = use('Validator')
const VenueRetriever = use('App/Utils/VenueRetriever')
const Pagination = require('./Helper/Pagination')
const Moment = require('moment')
const Exceptions = require('adonis-lucid/src/Exceptions')

class VenueController {
  * index (request, response) {
    const lat = request.input('lat')
    const lng = request.input('lng')
    const radius = request.input('radius', 10)
    const searchQuery = request.input('query')
    const time = Moment(request.input('time'))
    const section = request.input('section')
    const price = request.input('price')

    const validation = yield Validator.validate({section, searchQuery, lat, lng, radius, price}, {
      section: 'string|in:food,drinks,coffee,shops,arts,outdoors,sights',
      searchQuery: 'string',
      lat: 'range:-180,180',
      lng: 'range:-180,180',
      radius: 'range:0,6371',
      price: 'integer|range:1,5'
    })

    if (validation.fails()) {
      response.unprocessableEntity(validation.messages())
      return
    }

    const pagination = new Pagination(request)

    const cols = Venue.visibleList
    cols[0] = 'venues.id'

    // The main query
    const currentPageQuery = Venue.query().column(cols)

    // The query for calculation the total count
    const totalQuery = Venue.query()

    if (time.isValid()) {
      const formattedTime = time.format('YYYY-MM-DD HH:mm')

      currentPageQuery.innerJoin('venue_hours_ranges', 'venues.id', 'venue_hours_ranges.venue_id')
        .whereRaw('hours @> f_normalize_time(:time)', {time: formattedTime})

      totalQuery.innerJoin('venue_hours_ranges', 'venues.id', 'venue_hours_ranges.venue_id').whereRaw('hours @> f_normalize_time(:time)', {time: formattedTime})
    }

    if (searchQuery) {
      // The part of the query that is responsible for defining the similarity
      const similarityQueryString = 'similarity(name, :searchQuery) > :threshold'
      const queryParams = {
        searchQuery: searchQuery,
        threshold: 0
      }

      currentPageQuery.select(Database.raw(similarityQueryString + ' as similarity', {searchQuery: searchQuery}))
        .whereRaw(similarityQueryString, queryParams)
        .orderBy('similarity', 'DESC')

      totalQuery.whereRaw(similarityQueryString, queryParams)
    }

    if (lat !== null && lng !== null) {
      const validation = yield Validator.validate({lat, lng, radius}, {
        lat: 'required|range:-180,180',
        lng: 'required|range:-180,180',
        radius: 'required|range:0,6371'
      })

      if (validation.fails()) {
        response.unprocessableEntity(validation.messages())
        return
      }

      // Fetch the corresponding data from Foursquare
      yield VenueRetriever.retrieve(lat, lng, radius, section)

      // Calculate the distance between the search point and the venues's position
      currentPageQuery.select(Database.raw('(earth_distance(ll_to_earth(lat, lng), ll_to_earth(?, ?)) / 1000) as distance', [lat, lng]))

      // Check if location of the venue is in the circle around the search point
      // See https://www.postgresql.org/docs/8.3/static/earthdistance.html
      const inRadiusQuery = 'earth_box(ll_to_earth(?, ?), ?) @> ll_to_earth(lat, lng)'
      currentPageQuery.whereRaw(inRadiusQuery, [lat, lng, radius * 1000])
      currentPageQuery.orderBy('distance', 'asc')

      totalQuery.whereRaw(inRadiusQuery, [lat, lng, radius * 1000])
    }
    // Fetch the actual data and complete the Pagination object
    const totalResult = yield totalQuery.count().first()
    pagination.data = yield currentPageQuery.forPage(pagination.page, pagination.perPage)

    pagination.total = Number(totalResult.count)
    response.ok(pagination)
  }

  * show (request, response) {
    const venue = yield Venue
      .query()
      .where('id', request.param('id'))
      .with('category', 'hours')
      .scope('hours', (builder) => {
        builder.orderBy('hours', 'asc')
      })
      .first()

    if (!venue) {
      throw new Exceptions.ModelNotFoundException()
    }

    if (!venue.details_fetched) {
      yield VenueRetriever.retrieveDetails(venue)
    }

    response.ok(venue)
  }
}

module.exports = VenueController
