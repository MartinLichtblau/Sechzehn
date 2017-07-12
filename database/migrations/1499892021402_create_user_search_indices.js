'use strict'

const Schema = use('Schema')

class UsersTableSchema extends Schema {
  up () {
    this.raw('CREATE EXTENSION IF NOT EXISTS pg_trgm')
    this.raw('CREATE INDEX IF NOT EXISTS trgm_idx_username ON users USING gin (username gin_trgm_ops)')
    this.raw('CREATE INDEX IF NOT EXISTS trgm_idx_real_name ON users USING gin (real_name gin_trgm_ops)')
  }

  down () {
    this.table('users', (table) => {
      table.dropIndex('username', 'trgm_idx_username')
      table.dropIndex('username', 'trgm_idx_real_name')
    })
    this.raw('DROP EXTENSION IF EXISTS pg_trgm')
  }
}

module.exports = UsersTableSchema
