'use strict'

const Lucid = use('Lucid')

class CommentRating extends Lucid {
  comment () {
    return this.belongsTo('App/Model/Comment')
  }

  user () {
    return this.belongsTo('App/Model/User', 'username', 'username')
  }
}

module.exports = CommentRating
