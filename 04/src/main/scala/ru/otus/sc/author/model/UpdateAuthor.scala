package ru.otus.sc.author.model

import java.util.UUID

case class UpdateAuthorRequest(author: Author)

sealed trait UpdateAuthorResponse

object UpdateAuthorResponse {
  case class Updated(author: Author)    extends UpdateAuthorResponse
  case class NotFound(id: UUID)         extends UpdateAuthorResponse
  case object CantUpdateAuthorWithoutId extends UpdateAuthorResponse
}
