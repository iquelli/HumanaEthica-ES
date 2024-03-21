describe('Assessment', () => {
    beforeEach(() => {
        cy.deleteAllbutArs();
        cy.createDataForAssessmentsTest();
    });

    afterEach(() => {
        cy.deleteAllbutArs();
    });
})