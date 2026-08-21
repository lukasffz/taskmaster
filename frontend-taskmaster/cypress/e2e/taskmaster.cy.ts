describe('TaskMaster authentication and workspace', () => {
  const loginWith = (user: { email: string; password: string }) => {
    cy.clearCookies()
    cy.visit('/login')
    cy.get('input[type="email"]').type(user.email)
    cy.get('input[type="password"]').type(user.password)
    cy.contains('button', 'Entrar').click()
    cy.url().should('include', '/dashboard')
  }

  it('redirects unauthenticated users from protected routes', () => {
    cy.clearCookies()
    cy.visit('/dashboard')
    cy.url().should('include', '/login')
  })

  it('registers a user through the interface', () => {
    const uniqueId = `${Date.now()}-${Cypress._.random(1000, 9999)}`
    cy.visit('/register')
    cy.get('input[autocomplete="name"]').type(`Cadastro E2E ${uniqueId}`)
    cy.get('input[type="email"]').type(`cadastro-${uniqueId}@taskmaster.local`)
    cy.get('input[autocomplete="new-password"]').type('Taskmaster123!')
    cy.contains('button', 'Criar conta').click()
    cy.url().should('include', '/dashboard')
    cy.contains('Seu workspace').should('be.visible')
  })

  it('rejects invalid credentials and accepts valid credentials', () => {
    cy.registerApiUser().then((user) => {
      cy.clearCookies()
      cy.visit('/login')
      cy.get('input[type="email"]').type(user.email)
      cy.get('input[type="password"]').type('senha-incorreta')
      cy.contains('button', 'Entrar').click()
      cy.get('[role="alert"]').should('contain', 'Email ou senha inválidos.')
      loginWith(user)
      cy.contains(user.name.split(' ')[0]).should('be.visible')
    })
  })

  it('creates, edits, filters and deletes a project and its tasks', () => {
    cy.registerApiUser().then((user) => {
      loginWith(user)
      cy.contains('button', 'Criar projeto').click()
      cy.get('.modal input').type('Projeto E2E')
      cy.get('.modal textarea').type('Projeto criado pelo Cypress')
      cy.contains('button', 'Salvar projeto').click()
      cy.contains('Projeto E2E').should('be.visible')

      cy.contains('button', 'Editar').first().click()
      cy.get('.modal input').clear().type('Projeto E2E Atualizado')
      cy.contains('button', 'Salvar projeto').click()
      cy.contains('Projeto E2E Atualizado').should('be.visible')

      cy.contains('button', 'Nova tarefa').click()
      cy.get('.modal input').type('Tarefa E2E')
      cy.get('.modal textarea').type('Tarefa criada pelo Cypress')
      cy.contains('button', 'Salvar tarefa').click()
      cy.contains('Tarefa E2E').should('be.visible')

      cy.get('.task-card .card-actions button').first().click()
      cy.get('.modal select').select('DONE')
      cy.contains('button', 'Salvar tarefa').click()
      cy.contains('Concluída').should('be.visible')
      cy.contains('button', 'A fazer').click()
      cy.contains('Tarefa E2E').should('not.exist')
      cy.contains('button', 'Concluída').click()
      cy.contains('Tarefa E2E').should('be.visible')

      cy.on('window:confirm', () => true)
      cy.get('.task-card .card-actions button').last().click()
      cy.contains('Tarefa E2E').should('not.exist')
      cy.on('window:confirm', () => true)
      cy.get('[aria-label^="Excluir projeto"]').click()
      cy.contains('Projeto E2E Atualizado').should('not.exist')
    })
  })

  it('logs out and returns to the public login route', () => {
    cy.registerApiUser().then((user) => {
      loginWith(user)
      cy.contains('button', 'Sair').click()
      cy.url().should('include', '/login')
      cy.contains('Bem-vindo de volta.').should('be.visible')
    })
  })

  it('blocks a different user from accessing a project', () => {
    cy.registerApiUser().then((owner) => {
      cy.request({
        method: 'POST',
        url: `${Cypress.env('apiUrl')}/api/auth/login`,
        body: { email: owner.email, password: owner.password },
      })
      cy.request({
        method: 'POST',
        url: `${Cypress.env('apiUrl')}/api/projects`,
        body: { name: 'Projeto privado E2E', description: 'Ownership test' },
      }).then((projectResponse) => {
        const projectId = projectResponse.body.id
        cy.clearCookies()
        cy.registerApiUser().then((otherUser) => {
          cy.request({
            method: 'POST',
            url: `${Cypress.env('apiUrl')}/api/auth/login`,
            body: { email: otherUser.email, password: otherUser.password },
          })
          cy.request({
            method: 'GET',
            url: `${Cypress.env('apiUrl')}/api/projects/${projectId}`,
            failOnStatusCode: false,
          }).its('status').should('be.oneOf', [403, 404])
        })
      })
    })
  })

  it('redirects to login after the session cookie expires', () => {
    cy.registerApiUser().then((user) => {
      loginWith(user)
      cy.clearCookie('taskmaster_token')
      cy.visit('/dashboard')
      cy.url().should('include', '/login')
    })
  })
})
