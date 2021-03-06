package ru.otus.sc.model.author

import java.util.UUID

case class GetAuthorRequest(id: UUID)

sealed trait GetAuthorResponse

object GetAuthorResponse {
  case class Found(author: Author) extends GetAuthorResponse
  case class NotFound(id: UUID)    extends GetAuthorResponse
}
