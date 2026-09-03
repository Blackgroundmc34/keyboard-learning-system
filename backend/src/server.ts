const dotenv = require("dotenv");
const app = require("./app");
const { db } = require("./config/database");

dotenv.config();

const PORT = process.env.PORT || 3000;

async function startServer() {
  try {
    const connection = await db.getConnection();

    console.log("MySQL database connected successfully");

    connection.release();

    app.listen(PORT, () => {
      console.log(`API running on http://localhost:${PORT}`);
    });
  } catch (error) {
    console.error("Database connection failed:", error);
    process.exit(1);
  }
}

startServer();