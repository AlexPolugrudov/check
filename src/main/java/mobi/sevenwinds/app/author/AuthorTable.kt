package mobi.sevenwinds.app.author

import mobi.sevenwinds.app.budget.AuthorRecord
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object AuthorTable : IntIdTable("author") {
    val fullName = varchar("fullname", 255)
    val createdAt = datetime("created_at")
}
class AuthorEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<AuthorEntity>(AuthorTable)

    var fullname by AuthorTable.fullName
    var createdAt by AuthorTable.createdAt

    fun toResponse(): AuthorRecord {
        return AuthorRecord(fullname, createdAt)
    }
}