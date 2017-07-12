'use strict'

class Pagination {
  constructor (request) {
    let page = Number(request.input('page', 1))
    let perPage = Number(request.input('per_page', 10))

    if (isNaN(page) || page < 1) {
      page = 1
    }

    if (isNaN(perPage) || perPage < 1) {
      perPage = 10
    }

    this.page = page
    this.perPage = perPage
    this.total = 0
    this.data = null
  }

  toJSON () {
    return {
      total: Number(this.total),
      perPage: this.perPage,
      currentPage: this.page,
      lastPage: Math.ceil(this.total / this.perPage),
      data: this.data
    }
  }
}

module.exports = Pagination
