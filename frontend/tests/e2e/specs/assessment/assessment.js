describe('Assessment', () => {
  beforeEach(() => {
    cy.deleteAllButArs();
    cy.createDemoEntities();
    cy.createDataForAssessmentsTest();
  });

  afterEach(() => {
    cy.deleteAllButArs();
  });

  it('create assessment', () => {
    const REVIEW = 'review1234';
    const ACTIVITY_NAME = 'A1';

    cy.intercept('POST', '/institutions/*/assessments').as('createAssessment');

    cy.demoVolunteerLogin();
    cy.intercept('GET', '/activities').as('getActivities');
    cy.get('[data-cy="volunteerActivities"]').click();
    cy.wait('@getActivities');
    cy.get('[data-cy="volunteerActivitiesTable"] tbody tr').should(
      'have.length', 6);
    cy.get('[data-cy="volunteerActivitiesTable"] tbody tr')
      .eq(0).children().eq(0).should('contain', ACTIVITY_NAME);
    cy.get('[data-cy="volunteerActivitiesTable"] tbody tr')
        .get('[data-cy="writeAssessmentButton"]').eq(0).click();
    cy.get('[data-cy="reviewInput"]').type(REVIEW);
    cy.get('[data-cy="saveAssessment"]').click();
    cy.wait('@createAssessment');
    cy.logout();

    cy.demoMemberLogin();
    cy.intercept('GET', '/users/*/getInstitution').as('getInstitutions');
    cy.get('[data-cy="institution"]').click();
    cy.get('[data-cy="assessments"]').click();
    cy.wait('@getInstitutions');
    cy.get('[data-cy="institutionAssessmentsTable"] tbody tr')
      .should('have.length', 1);
    cy.get('[data-cy="institutionAssessmentsTable"] tbody tr')
      .eq(0).children().eq(0).should('contain', REVIEW);
    cy.logout();
  });
});
