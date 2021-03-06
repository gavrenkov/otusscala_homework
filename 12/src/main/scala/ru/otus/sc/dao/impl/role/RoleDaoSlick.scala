package ru.otus.sc.dao.impl.role

import java.util.UUID
import ru.otus.sc.dao.RoleDao
import ru.otus.sc.dao.impl.Slick.roles
import ru.otus.sc.model.role.Role
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.PostgresProfile.api._
import scala.concurrent.{ExecutionContext, Future}

class RoleDaoSlick(db: Database)(implicit ec: ExecutionContext) extends RoleDao {
  override def init(): Future[Unit] =
    db.run(
      DBIO.seq(
        roles.schema.createIfNotExists,
        roles.delete
      )
    )

  override def clean(): Future[Unit] = db.run(DBIO.seq(roles.delete))

  override def destroy(): Future[Unit] = db.run(DBIO.seq(roles.schema.dropIfExists))

  override def createRole(role: Role): Future[Role] = {
    val newId = UUID.randomUUID()
    val res = for {
      _ <- (roles returning roles) += RoleRow.fromRole(role).copy(id = Some(newId))
    } yield role.copy(id = Some(newId))

    db.run(res.transactionally)
  }

  override def getRole(id: UUID): Future[Option[Role]] = {
    val res = for {
      role <- roles.filter(role => role.id === id).result.headOption
    } yield role.map(_.toRole)

    db.run(res)
  }

  override def updateRole(role: Role): Future[Option[Role]] = {
    role.id match {
      case Some(roleId) =>
        val updateRole =
          roles
            .filter(_.id === roleId)
            .map(r => r.roleName)
            .update(role.name)

        val action =
          for {
            r <- roles.filter(_.id === roleId).forUpdate.result.headOption
            _ <- r match {
              case None    => DBIO.successful(())
              case Some(_) => updateRole
            }
          } yield r.map(_ => role)

        db.run(action.transactionally)

      case None => Future.successful(None)
    }
  }

  override def deleteRole(id: UUID): Future[Option[Role]] = {
    val action =
      for {
        r <- roles.filter(_.id === id).forUpdate.result.headOption
        res <- r match {
          case None => DBIO.successful(None)
          case Some(roleRow) =>
            for {
              _ <- roles.filter(_.id === id).delete
            } yield Some(roleRow.toRole)
        }
      } yield res

    db.run(action.transactionally)
  }

  private def findByCondition(condition: Roles => Rep[Boolean]): Future[Seq[Role]] =
    db.run(roles.filter(condition).result)
      .map(_.map { roleRow: RoleRow => roleRow.toRole }.toSeq)

  override def findByName(name: String): Future[Seq[Role]] = findByCondition(_.roleName === name)

  override def findAll(): Future[Seq[Role]] = findByCondition(_ => true)
}
