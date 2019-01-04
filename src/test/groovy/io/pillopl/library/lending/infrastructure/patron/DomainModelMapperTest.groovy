package io.pillopl.library.lending.infrastructure.patron

import io.pillopl.library.lending.domain.book.BookId
import io.pillopl.library.lending.domain.library.LibraryBranchId
import io.pillopl.library.lending.domain.patron.OverdueCheckouts
import io.pillopl.library.lending.domain.patron.PatronHolds
import io.pillopl.library.lending.domain.patron.PatronId
import io.pillopl.library.lending.domain.patron.PatronInformation
import spock.lang.Specification

import static io.pillopl.library.lending.domain.book.BookFixture.anyBookId
import static io.pillopl.library.lending.domain.book.BookFixture.bookOnHold
import static io.pillopl.library.lending.domain.library.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.domain.patron.PatronBooksFixture.anyPatronId
import static io.pillopl.library.lending.domain.patron.PatronInformation.PatronType.Regular
import static java.util.Collections.emptyList

class DomainModelMapperTest extends Specification {

    DomainModelMapper domainModelMapper = new DomainModelMapper()

    LibraryBranchId libraryBranchId = anyBranch()
    LibraryBranchId anotherBranchId = anyBranch()
    PatronId patronId = anyPatronId()
    BookId bookId = anyBookId()
    BookId anotherBookId = anyBookId()

    def 'should map patron information'() {
        given:
            PatronBooksDatabaseEntity entity = patronEntity(patronId, Regular)
        when:
            PatronInformation patronInformation = domainModelMapper.mapPatronInformation(entity)
        then:
            patronInformation.patronId == patronId
            patronInformation.type == Regular

    }

    def 'should map patron holds'() {
        given:
            PatronBooksDatabaseEntity entity = patronEntity(patronId, Regular, [
                    new BookOnHoldDatabaseEntity(bookId.bookId, patronId.patronId, libraryBranchId.libraryBranchId),
                    new BookOnHoldDatabaseEntity(anotherBookId.bookId, patronId.patronId, anotherBranchId.libraryBranchId)])
        when:
            PatronHolds patronHolds = domainModelMapper.mapPatronHolds(entity)
        then:
            patronHolds.count() == 2
            patronHolds.a(bookOnHold(bookId, libraryBranchId))
            patronHolds.a(bookOnHold(anotherBookId, anotherBranchId))

    }

    def 'should map patron overdue checkouts'() {
        given:
            PatronBooksDatabaseEntity entity = patronEntity(patronId, Regular, [], [
                    new OverdueCheckoutDatabaseEntity(bookId.bookId, patronId.patronId, libraryBranchId.libraryBranchId),
                    new OverdueCheckoutDatabaseEntity(anotherBookId.bookId, patronId.patronId, anotherBranchId.libraryBranchId)])
        when:
            OverdueCheckouts overdueCheckouts = domainModelMapper.mapPatronOverdueCheckouts(entity)
        then:
            overdueCheckouts.countAt(libraryBranchId) == 1
            overdueCheckouts.countAt(anotherBranchId) == 1
    }


    private PatronBooksDatabaseEntity patronEntity(PatronId patronId,
                                                   PatronInformation.PatronType type,
                                                   List<BookOnHoldDatabaseEntity> holds = emptyList(),
                                                   List<OverdueCheckoutDatabaseEntity> overdueCheckouts = emptyList()) {
        PatronBooksDatabaseEntity entity = new PatronBooksDatabaseEntity(new PatronInformation(patronId, type))
        entity.booksOnHold = holds as Set
        entity.overdueCheckouts = overdueCheckouts as Set
        return entity
    }

}
