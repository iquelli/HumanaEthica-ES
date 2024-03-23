
describe('Participation', () => {
    beforeEach(() => {
        cy.deleteAllButArs();
        cy.createDataForParticipations();
    });

    afterEach(() => {
        cy.deleteAllButArs();
    });

    it('create participation', () => {
        const RATING = 5;
        cy.intercept('POST', '/activities/*/participations').as('makeParticipant');

        cy.demoMemberLogin();
        cy.intercept('GET', '/users/*/getInstitution').as('getInstitutions');
        cy.intercept('GET', '/themes/availableThemes').as('availableThemes');
        cy.intercept('GET', '/activities/*/enrollments').as('getEnrollments');

        cy.get('[data-cy="institution"]').click();
        cy.get('[data-cy="activities"]').click()

        cy.wait('@getInstitutions');
        cy.wait('@availableThemes');

        //table has 2 instances and first activity has one participation
        cy.get('[data-cy="memberActivitiesTable"] tbody tr')
            .should('have.length', 2)
            .eq(0)
            .children()
            .eq(3)
            .should('contain', 1);

        cy.get('[data-cy="memberActivitiesTable"] tbody tr')
            .eq(0)
            .find('[data-cy="showEnrollments"]').click();
        cy.wait('@getEnrollments');

        //table has 2 instances and first enrollment isn't a participating one
        cy.get('[data-cy="activityEnrollmentsTable"] tbody tr')
            .should('have.length', 2)
            .eq(0)
            .children()
            .eq(2)
            .should('contain', false);

        //making first enrollment as participant
        cy.get('[data-cy="activityEnrollmentsTable"] tbody tr')
            .eq(0)
            .find('[data-cy="selectParticipant"]').click();
        cy.get('[data-cy="ratingInput"]').type(RATING);
        cy.get('[data-cy="makeParticipantButton"]').click();
        cy.wait('@makeParticipant');

        //first enrollment is now a participating one
        cy.get('[data-cy="activityEnrollmentsTable"] tbody tr')
            .eq(0)
            .children()
            .eq(2)
            .should('contain', true);

        cy.get('[data-cy="getActivities"]').click();

        //first activity has now two participations
        cy.get('[data-cy="memberActivitiesTable"] tbody tr')
            .eq(0)
            .children()
            .eq(3)
            .should('contain', 2);
        cy.logout()
    });
});
        
        



