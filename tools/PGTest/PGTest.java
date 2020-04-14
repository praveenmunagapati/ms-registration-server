import java.sql.*;
import java.util.LinkedList;
import java.util.List;

public class PGTest
{
    public static void main(String[] args) throws SQLException
    {
        try (Connection conn = (3 == args.length ? DriverManager.getConnection(args[0], args[1], args[2]) : DriverManager.getConnection("jdbc:postgresql://" + args[0] + ":" + args[1] + "/" + args[2], args[3], args[4])))
        {
            DatabaseMetaData dmd = conn.getMetaData();
            System.out.println(dmd.getDriverName() + " " + dmd.getDriverVersion());
            System.out.println(dmd.getDatabaseProductName() + " " + dmd.getDatabaseProductVersion() + " database \"" + conn.getCatalog() + "\"");

            testTimestampAdd(conn);

            System.out.flush();
            System.out.close();
        }
    }

    private static void testTimestampAdd(Connection conn) throws SQLException
    {
        testTimestampAdd("SQL_TSI_SECOND", conn);
        testTimestampAdd("SQL_TSI_MINUTE", conn);
        testTimestampAdd("SQL_TSI_HOUR", conn);
    }

    private static void testTimestampAdd(String interval, Connection conn) throws SQLException
    {
        String sql = "SELECT {fn timestampadd(" + interval + ", 3, CAST('01 Jan 2003' AS TIMESTAMP))} AS AddThree";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql))
        {
            while (rs.next())
                System.out.println(interval + ": " + rs.getTimestamp(1));
        }
    }

    // This is now illegal, as of driver version 9.4.1208
    private static void testScalarFunctionsInFunctionDefinitions(Connection conn) throws SQLException
    {
        try (PreparedStatement stmt = conn.prepareStatement(
                "CREATE FUNCTION public.fn_now() RETURNS VOID AS $$\n" +
                        "    BEGIN\n" +
                        "        SELECT {fn now()};" +
                        "    END;\n" +
                        "$$ LANGUAGE plpgsql;" +
                        "DROP FUNCTION public.fn_now();")) {
            stmt.execute();
        }
    }

    // See https://www.labkey.org/home/Developer/issues/issues-details.view?issueId=27534
    // We expect this to log errors for "/*" and "*/". All other combinations should succeed.
    private static void testJdbcParsingBug(Connection conn)
    {
        List<String> badStrings = new LinkedList<>();

        testJdbcParsingBug(conn, "", badStrings);
        testJdbcParsingBug(conn, " ", badStrings);
        testJdbcParsingBug(conn, "\n", badStrings);
        testJdbcParsingBug(conn, "\t", badStrings);
        testJdbcParsingBug(conn, "/* */", badStrings);
        testJdbcParsingBug(conn, "/*", badStrings);
        testJdbcParsingBug(conn, "/* $ */", badStrings);

        // Test one-character injections
        for (char c1 = 1; c1 < 256; c1++)
        {
            testJdbcParsingBug(conn, String.valueOf(c1), badStrings);
        }

        // Test two-character injections
        for (char c1 = 1; c1 < 256; c1++)
        {
            foo: for (char c2 = 1; c2 < 256; c2++)
            {
                String inject = String.valueOf(c1) + String.valueOf(c2);

                for (String bad : badStrings)
                    if (inject.contains(bad))
                        continue foo;

                testJdbcParsingBug(conn, inject, badStrings);
            }
        }

//        for (char c1 = 1; c1 < 256; c1++)
//        {
//            for (char c2 = 1; c2 < 256; c2++)
//            {
//                bar: for (char c3 = 1; c3 < 256; c3++) {
//                    String inject = String.valueOf(c1) + String.valueOf(c2) + String.valueOf(c3);
//
//                    for (String bad : badStrings)
//                        if (inject.contains(bad))
//                            continue bar;
//
//                    testJdbcParsingBug(conn, inject, badStrings);
//                }
//            }
//        }
    }

    private static boolean testJdbcParsingBug(Connection conn, String inject, List<String> badStrings)
    {
        String sql = "/* " + inject + " */ SELECT {fn now()}";
        try (PreparedStatement stmt = conn.prepareStatement(sql))
        {
            stmt.execute();
            return true;
        }
        catch (Throwable t)
        {
            System.out.println(sql + " generated " + t.toString());
        }

        badStrings.add(inject);
        return false;
    }

    // An attempt to repro statement caching problems in recent PG drivers (but it doesn't repro)
    private static void testStatementCaching(Connection conn) throws SQLException
    {
        try
        {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "CREATE TABLE public.Test_Caching" +
                            "(" +
                            "Name VARCHAR(100)," +
                            "Age INTEGER," +
                            "Birth TIMESTAMP" +
                            ");" +
                            "")) {
                stmt.execute();
            }
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT Name, Age, Birth FROM public.Test_Caching;")) {
                stmt.execute();
            }
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT * FROM public.Test_Caching;")) {
                stmt.execute();
            }
            try (PreparedStatement stmt = conn.prepareStatement(
                    "ALTER TABLE public.Test_Caching ALTER COLUMN Name TYPE VARCHAR(10);")) {
                stmt.execute();
            }
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT Name, Age, Birth FROM public.Test_Caching;")) {
                stmt.execute();
            }
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT * FROM public.Test_Caching;")) {
                stmt.execute();
            }
            try (PreparedStatement stmt = conn.prepareStatement(
                    "ALTER TABLE public.Test_Caching DROP COLUMN Name;ALTER TABLE public.Test_Caching ADD COLUMN Name INTEGER;")) {
                stmt.execute();
            }
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT Name, Age, Birth FROM public.Test_Caching;")) {
                stmt.execute();
            }
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT * FROM public.Test_Caching;")) {
                stmt.execute();
            }
        }
        finally
        {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "DROP TABLE IF EXISTS public.Test_Caching;")) {
                stmt.execute();
            }
        }
    }
}
