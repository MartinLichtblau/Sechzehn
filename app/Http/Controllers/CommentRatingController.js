'use strict'

const CommentRating = use('App/Model/CommentRating')
const Validator = use('Validator')
const Comment = use('App/Model/Comment')

class CommenRatingController {
  * store (request, response) {
    const user = request.authUser
    const comment = yield Comment.findOrFail(request.param('comment_id'))
    let thumbsUp = request.input('thumbs_up')

    const validation = yield Validator.validate({thumbsUp}, {
      thumbsUp: 'required'
    })

    if (validation.fails()) {
      response.unprocessableEntity(validation.messages())
      return
    }

    thumbsUp = Validator.sanitizor.toBoolean(thumbsUp)

    const oldCommentRating = yield CommentRating.query().where('username', user.username).where('comment_id', comment.id).first()

    if (oldCommentRating) {
      oldCommentRating.thumbs_up = thumbsUp
      yield oldCommentRating.save()
    } else {
      yield CommentRating.create({
        username: user.username,
        comment_id: comment.id,
        thumbs_up: thumbsUp
      })
    }

    response.ok(comment)
  }
}

module.exports = CommenRatingController
