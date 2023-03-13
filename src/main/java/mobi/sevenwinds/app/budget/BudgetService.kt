package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mobi.sevenwinds.app.author.AuthorEntity
import mobi.sevenwinds.app.author.AuthorTable
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object BudgetService {
    suspend fun addRecord(body: BudgetRecord): BudgetRecord = withContext(Dispatchers.IO) {
        transaction {
            val entity = BudgetEntity.new {
                this.year = body.year
                this.month = body.month
                this.amount = body.amount
                this.type = body.type
                this.authorId = body.authorId
            }

            return@transaction entity.toResponse()
        }
    }


    suspend fun getYearStats(param: BudgetYearParam): BudgetYearStatsResponse = withContext(Dispatchers.IO) {
        transaction {
            var query = BudgetTable
                .join(AuthorTable, JoinType.LEFT, additionalConstraint = {BudgetTable.authorId eq AuthorTable.id})
                .select { BudgetTable.year eq param.year }
                .limit(param.limit, param.offset)

            if (!param.author.isNullOrBlank()) {
                query = query.andWhere { AuthorTable.fullName.lowerCase().like("%{param.author.toLowerCase()}%") }
            }

            val total = query.count()
            val data = BudgetEntity.wrapRows(query).map {
                BudgetRecordWithAuthor(
                    it.id.value,
                    it.year,
                    it.month,
                    it.amount,
                    it.type,
                    it.authorId,
                    it.authorId?.let { authorId ->
                        AuthorEntity[authorId].fullname
                    },
                    it.authorId?.let { authorId ->
                        AuthorEntity[authorId].createdAt
                    }
                )
            }

            val sumByType = data.groupBy { it.type.name }.mapValues { it.value.sumOf { v -> v.amount } }

            return@transaction BudgetYearStatsResponse(
                total = total,
                totalByType = sumByType,
                items = data
            )
        }
    }
}