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
    const INITIAL_APPLICATIONS = '0';
    const MOTIVATION = 'motivation';
    const FINAL_APPLICATIONS = '1';

    cy.intercept('GET', '/users/*/getInstitution').as('getInstitutions');

    cy.demoMemberLogin();
    cy.get('[data-cy="institution"]').click();
    cy.get('[data-cy="activities"]').click();
    cy.wait('@getInstitutions');
    cy.get('[data-cy="memberActivitiesTable"] tbody tr')
      .should('have.length', 3)
      .eq(0)
      .children()
      .eq(3)
      .should('contain', INITIAL_APPLICATIONS);
    cy.logout();

    cy.demoVolunteerLogin();
    cy.intercept('POST', '/activities/*/enrollments').as('createEnrollment');
    cy.intercept('GET', '/activities').as('getActivities');
    cy.get('[data-cy="volunteerActivities"]').click();
    cy.wait('@getActivities');
    cy.get('[data-cy="volunteerActivitiesTable"] tbody tr')
      .get('[data-cy="applyForActivityButton"]')
      .eq(0)
      .click();
    cy.get('[data-cy="motivationInput"]').type(MOTIVATION);
    cy.get('[data-cy="saveEnrollment"]').click();
    cy.wait('@createEnrollment');
    cy.logout();

    cy.demoMemberLogin();
    cy.intercept('GET', '/activities/*/enrollments').as('getEnrollments');
    cy.get('[data-cy="institution"]').click();
    cy.get('[data-cy="activities"]').click();
    cy.wait('@getInstitutions');
    cy.get('[data-cy="memberActivitiesTable"] tbody tr')
      .eq(0)
      .children()
      .eq(3)
      .should('contain', FINAL_APPLICATIONS);
    cy.get('[data-cy="memberActivitiesTable"] tbody tr')
      .get('[data-cy="showEnrollments"]')
      .eq(0)
      .click();
    cy.wait('@getEnrollments');
    cy.get('[data-cy="activityEnrollmentsTable"] tbody tr')
      .should('have.length', 1)
      .eq(0)
      .children()
      .eq(1)
      .should('contain', MOTIVATION);
    cy.logout();
  });
});
