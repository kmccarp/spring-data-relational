/*
 * Copyright 2020-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.jdbc.testing;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.testcontainers.containers.OracleContainer;

/**
 * {@link DataSource} setup for Oracle Database XE. Starts a docker container with an Oracle database.
 *
 * @see <a href=
 *      "https://blogs.oracle.com/oraclemagazine/deliver-oracle-database-18c-express-edition-in-containers">Oracle
 *      Docker Image</a>
 * @see <a href="https://www.testcontainers.org/modules/databases/oraclexe/">Testcontainers Oracle</a>
 * @author Thomas Lang
 * @author Jens Schauder
 */
@Configuration
@Profile("oracle")
public class OracleDataSourceConfiguration extends DataSourceConfiguration {

	private static final Log LOG = LogFactory.getLog(OracleDataSourceConfiguration.class);

	private static OracleContainer oracleContainer;

	@Override
	protected DataSource createDataSource() {

		if (oracleContainer == null) {

			LOG.info("Oracle starting...");
			OracleContainer container = new OracleContainer("gvenzl/oracle-xe:21.3.0-slim").withReuse(true);
			container.start();
			LOG.info("Oracle started");

			oracleContainer = container;
		}

		initDb();

		return new DriverManagerDataSource(oracleContainer.getJdbcUrl(), oracleContainer.getUsername(),
				oracleContainer.getPassword());
	}

	private void initDb() {

		final DriverManagerDataSource dataSource = new DriverManagerDataSource(oracleContainer.getJdbcUrl(), "SYSTEM",
				oracleContainer.getPassword());
		final JdbcTemplate jdbc = new JdbcTemplate(dataSource);
		jdbc.execute("GRANT ALL PRIVILEGES TO " + oracleContainer.getUsername());
	}

	@Override
	protected void customizePopulator(ResourceDatabasePopulator populator) {
		populator.setIgnoreFailedDrops(true);
	}
}
