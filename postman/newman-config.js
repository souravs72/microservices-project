#!/usr/bin/env node

/**
 * Newman Configuration for Microservices API Testing
 *
 * This script configures Newman (command-line Postman) for automated API testing
 * in CI/CD pipelines and local development environments.
 *
 * Usage:
 *   node newman-config.js
 *   npm run test:api
 *   npm run test:api:ci
 */

const newman = require("newman");
const path = require("path");

// Configuration options
const config = {
  // Collection files
  collections: {
    main: path.join(__dirname, "Microservices_API_Collection.json"),
    scenarios: path.join(__dirname, "API_Testing_Scenarios.json"),
  },

  // Environment files
  environments: {
    development: path.join(__dirname, "Development_Environment.json"),
    docker: path.join(__dirname, "Docker_Environment.json"),
    production: path.join(__dirname, "Production_Environment.json"),
  },

  // Report configurations
  reports: {
    html: {
      enabled: true,
      output: "./test-reports/html-report.html",
      template: "./test-reports/html-template.hbs",
    },
    json: {
      enabled: true,
      output: "./test-reports/json-report.json",
    },
    junit: {
      enabled: true,
      output: "./test-reports/junit-report.xml",
    },
    cli: {
      enabled: true,
      verbose: true,
    },
  },

  // Test execution options
  execution: {
    timeout: 30000,
    delayRequest: 1000,
    iterationCount: 1,
    bail: false,
    suppressExitCode: false,
  },
};

/**
 * Run Newman tests with specified configuration
 */
function runTests(collection, environment, options = {}) {
  const newmanOptions = {
    collection: collection,
    environment: environment,
    timeout: options.timeout || config.execution.timeout,
    delayRequest: options.delayRequest || config.execution.delayRequest,
    iterationCount: options.iterationCount || config.execution.iterationCount,
    bail: options.bail || config.execution.bail,
    suppressExitCode:
      options.suppressExitCode || config.execution.suppressExitCode,
    reporters: ["cli"],
    reporter: {
      cli: {
        verbose: true,
        noSummary: false,
        noBanner: false,
        noAssertions: false,
      },
    },
  };

  // Add HTML reporter if enabled
  if (config.reports.html.enabled) {
    newmanOptions.reporters.push("html");
    newmanOptions.reporter.html = {
      export: config.reports.html.output,
    };
  }

  // Add JSON reporter if enabled
  if (config.reports.json.enabled) {
    newmanOptions.reporters.push("json");
    newmanOptions.reporter.json = {
      export: config.reports.json.output,
    };
  }

  // Add JUnit reporter if enabled
  if (config.reports.junit.enabled) {
    newmanOptions.reporters.push("junit");
    newmanOptions.reporter.junit = {
      export: config.reports.junit.output,
    };
  }

  return new Promise((resolve, reject) => {
    newman.run(newmanOptions, (err, summary) => {
      if (err) {
        console.error("Newman execution error:", err);
        reject(err);
      } else {
        console.log("Newman execution completed");
        console.log("Summary:", JSON.stringify(summary, null, 2));
        resolve(summary);
      }
    });
  });
}

/**
 * Run all test scenarios
 */
async function runAllTests() {
  console.log("🚀 Starting comprehensive API test suite...\n");

  const results = {};

  try {
    // Test main collection with development environment
    console.log("📋 Running main API collection...");
    results.main = await runTests(
      config.collections.main,
      config.environments.development,
      { iterationCount: 1 }
    );

    // Test scenarios with development environment
    console.log("\n🧪 Running API testing scenarios...");
    results.scenarios = await runTests(
      config.collections.scenarios,
      config.environments.development,
      { iterationCount: 1 }
    );

    console.log("\n✅ All tests completed successfully!");
    return results;
  } catch (error) {
    console.error("\n❌ Test execution failed:", error.message);
    process.exit(1);
  }
}

/**
 * Run tests for specific environment
 */
async function runTestsForEnvironment(env) {
  const environmentFile = config.environments[env];

  if (!environmentFile) {
    console.error(`❌ Environment '${env}' not found`);
    console.log("Available environments:", Object.keys(config.environments));
    process.exit(1);
  }

  console.log(`🌍 Running tests for ${env} environment...`);

  try {
    const results = await runTests(config.collections.main, environmentFile, {
      iterationCount: 1,
    });

    console.log(`✅ Tests completed for ${env} environment`);
    return results;
  } catch (error) {
    console.error(`❌ Tests failed for ${env} environment:`, error.message);
    process.exit(1);
  }
}

/**
 * Run performance tests
 */
async function runPerformanceTests() {
  console.log("⚡ Running performance tests...");

  try {
    const results = await runTests(
      config.collections.scenarios,
      config.environments.development,
      {
        iterationCount: 10,
        delayRequest: 100,
        timeout: 60000,
      }
    );

    console.log("✅ Performance tests completed");
    return results;
  } catch (error) {
    console.error("❌ Performance tests failed:", error.message);
    process.exit(1);
  }
}

/**
 * Generate test reports
 */
function generateReports() {
  console.log("📊 Generating test reports...");

  const fs = require("fs");
  const path = require("path");

  // Ensure reports directory exists
  const reportsDir = path.dirname(config.reports.html.output);
  if (!fs.existsSync(reportsDir)) {
    fs.mkdirSync(reportsDir, { recursive: true });
  }

  console.log(`📁 Reports will be saved to: ${reportsDir}`);
  console.log(`📄 HTML Report: ${config.reports.html.output}`);
  console.log(`📄 JSON Report: ${config.reports.json.output}`);
  console.log(`📄 JUnit Report: ${config.reports.junit.output}`);
}

/**
 * CLI interface
 */
function main() {
  const args = process.argv.slice(2);
  const command = args[0];

  switch (command) {
    case "all":
      runAllTests();
      break;

    case "dev":
      runTestsForEnvironment("development");
      break;

    case "docker":
      runTestsForEnvironment("docker");
      break;

    case "prod":
      runTestsForEnvironment("production");
      break;

    case "performance":
      runPerformanceTests();
      break;

    case "reports":
      generateReports();
      break;

    case "help":
    case "--help":
    case "-h":
      console.log(`
🚀 Newman API Testing Configuration

Usage: node newman-config.js <command>

Commands:
  all          Run all test collections
  dev          Run tests with development environment
  docker       Run tests with docker environment
  prod         Run tests with production environment
  performance  Run performance tests
  reports      Generate test reports
  help         Show this help message

Examples:
  node newman-config.js all
  node newman-config.js dev
  node newman-config.js performance

Environment Variables:
  NEWMAN_TIMEOUT        Test timeout in milliseconds (default: 30000)
  NEWMAN_ITERATIONS     Number of iterations (default: 1)
  NEWMAN_REPORTS_DIR    Reports output directory (default: ./test-reports)
      `);
      break;

    default:
      console.log("❌ Unknown command:", command);
      console.log('Run "node newman-config.js help" for usage information');
      process.exit(1);
  }
}

// Export for use as module
module.exports = {
  config,
  runTests,
  runAllTests,
  runTestsForEnvironment,
  runPerformanceTests,
  generateReports,
};

// Run CLI if called directly
if (require.main === module) {
  main();
}
