import { NextRequest, NextResponse } from "next/server";

export const revalidate = 0;

export async function GET(request: NextRequest) {
    try {
        // For now, we always use congress 119
        const fixedCongressNumber = "119";

        // Build your Spring Boot URL
        const backendUrl = `${process.env.BACKEND_BASE_URL}/api/internal/control/${fixedCongressNumber}`;
        console.log(`Fetching congress data from: ${backendUrl}`);

        // "fetch" with revalidation can also be configured here, if desired:
        const response = await fetch(backendUrl, {
            // If you want the fetched data to be cached/revalidated, too:
            next: { revalidate: 86400 },
        });

        if (!response.ok) {
            const errorText = await response.text();
            console.error("Error from backend (congress data):", errorText);
            return NextResponse.json({ error: "Failed to fetch congress data" }, { status: response.status });
        }

        // Return the JSON data as-is
        const data = await response.json();
        return NextResponse.json(data);

    } catch (error) {
        console.error("Error fetching congress data:", error);
        return NextResponse.json({ error: "Internal server error" }, { status: 500 });
    }
}
