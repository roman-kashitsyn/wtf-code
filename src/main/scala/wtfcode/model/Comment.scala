package wtfcode.model

import net.liftweb.mapper._

class Comment extends LongKeyedMapper[Comment] with IdPK with CreatedTrait
with SaveIP with Rated with ManyToMany {
  def getSingleton = Comment

  override val createdAtIndexed_? = true

  object author extends MappedLongForeignKey(this, User)
  object post extends MappedLongForeignKey(this, Post)
  object content extends MappedText(this)
  object rating extends MappedInt(this) {
    override def defaultValue = 0
  }

  object votes extends MappedManyToMany(CommentVote, CommentVote.comment, CommentVote.user, User)

  def anchor = "comment_" + id
  def link = post.foreign.map(_.link).openOr("") + "#" + anchor

  override def currentRating = this.rating.is
  override def canVote(user: User) =
    author != user &&
      CommentVote.count(By(CommentVote.comment, this), By(CommentVote.user, user)) == 0

  override protected def updateVotes(user: User, func: Int => Int): Int = {
    CommentVote.create.comment(this).user(user).save()
    rating(func(rating))
    save
    rating
  }
}

object Comment extends Comment with LongKeyedMetaMapper[Comment] {
  override def dbTableName = "comments"
}