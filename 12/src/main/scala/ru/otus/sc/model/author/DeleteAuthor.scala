package ru.otus.sc.model.author

import java.util.UUID

case class DeleteAuthorRequest(id: UUID)

sealed trait DeleteAuthorResponse

object DeleteAuthorResponse {
  case class Deleted(author: Author) extends DeleteAuthorResponse
  case class NotFound(id: UUID)      extends DeleteAuthorResponse
}
