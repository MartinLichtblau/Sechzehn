'use strict'

const Database = use('Database')
const Exceptions = require('adonis-lucid/src/Exceptions')
const Moment = require('moment')
const Pagination = require('./Helper/Pagination')
const User = use('App/Model/User')
const Validator = use('Validator')
const Venue = use('App/Model/Venue')
const VenueRetriever = use('App/Utils/VenueRetriever')

class VenueController {
  * index (request, response) {
    const lat = request.input('lat')
    const lng = request.input('lng')
    const radius = request.input('radius', 10)
    const searchQuery = request.input('query')
    const time = Moment(request.input('time'))
    const section = request.input('section', '')
    const price = Number(request.input('price'))
    const sortByDistance = Validator.sanitizor.toBoolean(request.input('sort_by_distance'))

    const validation = yield Validator.validate({section, searchQuery, lat, lng, radius, price}, {
      section: 'string|in:food,drinks,coffee,shops,arts,outdoors,sights',
      searchQuery: 'string',
      lat: 'range:-180,180',
      lng: 'range:-180,180',
      radius: 'range:0,6371',
      price: 'integer|range:-1,6' // exclusive bounds
    })

    if (validation.fails()) {
      response.unprocessableEntity(validation.messages())
      return
    }

    const pagination = new Pagination(request)

    const cols = Venue.visibleList
    cols.push('venue_categories.name as category')
    // Calculate the rating based on the foursquare rating and the ratings from the CheckIns
    cols.push(Database.raw('round(((COALESCE(checkins_rating, foursquare_rating, -1) + COALESCE(foursquare_rating, checkins_rating, -1)) / 2)::numeric, 2) as rating'))
    // Determine the overall rating count
    cols.push(Database.raw('(COALESCE(foursquare_rating_count, 0) + COALESCE(checkins_rating_count, 0)) as rating_count'))

    // The main query
    const currentPageQuery = Venue.query().column(cols).leftOuterJoin('venue_categories', 'venues.category_id', 'venue_categories.id')
      .leftOuterJoin(Venue.ratingQuery, 'rating_query.venue_id', 'venues.id')

    // The query for calculation the total count
    const totalQuery = Venue.query().leftOuterJoin('venue_categories', 'venues.category_id', 'venue_categories.id')

    if (time.isValid()) {
      const formattedTime = time.format('YYYY-MM-DD HH:mm')

      currentPageQuery.innerJoin('venue_hours_ranges', 'venues.id', 'venue_hours_ranges.venue_id')
        .whereRaw('hours @> f_normalize_time(:time)', {time: formattedTime})

      totalQuery.innerJoin('venue_hours_ranges', 'venues.id', 'venue_hours_ranges.venue_id').whereRaw('hours @> f_normalize_time(:time)', {time: formattedTime})
    }

    if (searchQuery) {
      // The part of the query that is responsible for defining the similarity
      const similarityQueryString = 'GREATEST(similarity(venues.name, :searchQuery), similarity(venue_categories.name, :searchQuery))'
      const queryParams = {
        searchQuery: searchQuery,
        threshold: 0
      }

      currentPageQuery.select(Database.raw(similarityQueryString + ' as similarity', {searchQuery: searchQuery}))
        .whereRaw(similarityQueryString + ' > :threshold', queryParams)

      if (!sortByDistance) {
        currentPageQuery.orderBy('similarity', 'DESC')
      }

      totalQuery.whereRaw(similarityQueryString + ' > :threshold', queryParams)
    }

    if (price && price !== 0) {
      currentPageQuery.where('price', price)
      totalQuery.where('price', price)
    }

    if (section && section !== '') {
      currentPageQuery.innerJoin('venue_section', 'venues.id', 'venue_section.venue_id')
      currentPageQuery.where('section', section)

      totalQuery.innerJoin('venue_section', 'venues.id', 'venue_id')
      totalQuery.where('section', section)
    }

    if (lat !== null && lng !== null) {
      // Fetch the corresponding data from Foursquare
      yield VenueRetriever.retrieve(lat, lng, radius, section)

      // Calculate the distance between the search point and the venues's position
      currentPageQuery.select(Database.raw('(earth_distance(ll_to_earth(lat, lng), ll_to_earth(?, ?)) / 1000) as distance', [lat, lng]))

      // Check if location of the venue is in the circle around the search point
      // See https://www.postgresql.org/docs/8.3/static/earthdistance.html
      const inRadiusQuery = 'earth_box(ll_to_earth(?, ?), ?) @> ll_to_earth(lat, lng)'
      currentPageQuery.whereRaw(inRadiusQuery, [lat, lng, radius * 1000])

      if (sortByDistance) {
        currentPageQuery.orderBy('distance', 'asc')
      }

      totalQuery.whereRaw(inRadiusQuery, [lat, lng, radius * 1000])
    }

    if (!(sortByDistance && lat !== null && lng !== null) && !searchQuery) {
      currentPageQuery.orderBy('rating', 'desc')
    }

    // Fetch the actual data and complete the Pagination object
    const totalResult = yield totalQuery.count().first()
    pagination.data = yield currentPageQuery.forPage(pagination.page, pagination.perPage)
    pagination.data.map(item => {
      item.rating = Number(item.rating)
      item.rating_count = Number(item.rating_count)
      item.category = {
        name: item.category
      }
    })

    pagination.total = Number(totalResult.count)
    response.ok(pagination)
  }

  * show (request, response) {
    const venue = yield Venue
      .query()
      .where('venues.id', request.param('id'))
      .with('category', 'hours', 'checkins', 'checkins.user')
      .scope('hours', (builder) => {
        builder.orderBy('hours', 'asc')
      })
      .scope('checkins', builder => {
        builder.orderBy('created_at', 'desc')
        builder.limit(3)
      })
      .leftOuterJoin(Venue.ratingQuery, 'rating_query.venue_id', 'venues.id')
      .first()

    if (!venue) {
      throw new Exceptions.ModelNotFoundException()
    }

    if (!venue.details_fetched) {
      yield VenueRetriever.retrieveDetails(venue)
    }

    const userColumns = User.visible.map(item => 'users.' + item)
    userColumns.push(Database.raw('count(id) as visit_count'))

    const topVisitors = yield Database.select(userColumns).from('check_ins')
      .where('venue_id', venue.id).groupBy('users.username')
      .innerJoin('users', 'check_ins.username', 'users.username')
      .orderBy('visit_count', 'desc').limit(3)

    venue.top_visitors = topVisitors.map(item => {
      const visitCount = Number(item.visit_count)
      delete item.visit_count

      return {
        user: item,
        visit_count: visitCount
      }
    })

    response.ok(venue)
  }
}

module.exports = VenueController
