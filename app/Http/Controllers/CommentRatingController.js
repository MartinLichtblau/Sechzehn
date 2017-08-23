'use strict'

const Comment = use('App/Model/Comment')
const CommentRating = use('App/Model/CommentRating')
const Database = use('Database')
const Validator = use('Validator')

class CommenRatingController {
  * store (request, response) {
    const user = request.authUser
    const comment = yield Comment.findOrFail(request.param('comment_id'))
    const rating = Number(request.input('rating', 0))

    const validation = yield Validator.validate({rating}, {
      rating: 'required|integer|range:-2,2'
    })

    if (validation.fails()) {
      response.unprocessableEntity(validation.messages())
      return
    }

    const oldCommentRating = yield CommentRating.query()
      .where('username', user.username)
      .where('comment_id', comment.id).first()

    if (oldCommentRating) {
      oldCommentRating.rating = rating
      yield oldCommentRating.save()
    } else {
      yield CommentRating.create({
        username: user.username,
        comment_id: comment.id,
        rating
      })
    }

    const ratingQueryResult = yield Database.select(Database.raw('sum(rating) as rating')).from('comment_ratings').where('comment_id', comment.id).first()

    comment.rating = ratingQueryResult.rating

    response.ok(comment)
  }
}

module.exports = CommenRatingController
