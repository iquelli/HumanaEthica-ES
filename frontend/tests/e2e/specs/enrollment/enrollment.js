describe('Enrollment', () => {
    beforeEach(() => {
        cy.deleteAllButArs();
        cy.createDemoEntities();
        cy.createDataForEnrollmentsTest();
    });

    afterEach(() => {
        cy.deleteAllButArs();
    });

    it('create enrollment', () => {
        const MOTIVATION = 'motivation';

        cy.intercept('POST', '/activities/*/enrollments').as('createEnrollment');

        //Tiago part

        cy.demoVolunteerLogin();
        cy.intercept('GET', '/activities').as('getActivities');
        cy.get('[data-cy="volunteerActivities"]').click();
        cy.wait('@getActivities');
        cy.get('[data-cy="volunteerActivitiesTable"] tbody tr')
            .get('[data-cy="applyForActivityButton"]').eq(0).click();
        cy.get('[data-cy="motivationInput"]').type(MOTIVATION);
        cy.get('[data-cy="saveEnrollment"]').click();
        cy.wait('@createEnrollment');
        cy.logout();

        //Tiago part

    });
});