Cypress.Commands.add('apiUrl', () => Cypress.env('apiUrl') as string)

Cypress.Commands.add('registerApiUser', () => {
  const uniqueId = `${Date.now()}-${Cypress._.random(1000, 9999)}`
  const user = {
    name: `E2E User ${uniqueId}`,
    email: `e2e-${uniqueId}@taskmaster.local`,
    password: 'Taskmaster123!',
  }

  return cy.request({
    method: 'POST',
    url: `${Cypress.env('apiUrl')}/api/auth/register`,
    body: user,
  }).then(() => user)
})

declare global {
  namespace Cypress {
    interface Chainable {
      apiUrl(): Chainable<string>
      registerApiUser(): Chainable<{ name: string; email: string; password: string }>
    }
  }
}

export {}
