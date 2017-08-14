'use strict'

const Venue = use('App/Model/Venue')
const Database = use('Database')
const Validator = use('Validator')
const VenueRetriever = use('App/Utils/VenueRetriever')
const Pagination = require('./Helper/Pagination')
const Moment = require('moment')

class VenueController {
  * index (request, response) {
    const lat = request.input('lat')
    const lng = request.input('lng')
    const radius = request.input('radius', 10)
    const searchQuery = request.input('query')
    const time = Moment(request.input('time'))

    yield VenueRetriever.retrieve(lat, lng, radius)

    const pagination = new Pagination(request)

    const cols = Venue.visible

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
    const venue = yield Venue.findOrFail(request.param('id'))

    if (!venue.details_fetched) {
      yield VenueRetriever.retrieveDetails(venue)
    }

    // ugly workaround the restrictions of the framwork
    const data = venue.detailView()
    data.category = yield venue.category().fetch()

    response.ok(data)
  }
}

module.exports = VenueController
