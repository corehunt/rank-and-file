import { NextRequest, NextResponse } from "next/server";

export const revalidate = 0;

export async function GET(request: NextRequest) {
    try {
        // Extract all query params from the request URL
        const { searchParams } = new URL(request.url);
        const queryString = searchParams.toString();

        // Build the backend URL
        const backendUrl = `${process.env.BACKEND_BASE_URL}/api/internal/bill/search?${queryString}`;
        console.log(`Fetching bills (search) from: ${backendUrl}`);

        const response = await fetch(backendUrl);
        if (!response.ok) {
            const errorText = await response.text();
            console.error("Error from backend search:", errorText);
            return NextResponse.json({ error: "Failed to fetch search results" }, { status: response.status });
        }

        const data = await response.json();
        return NextResponse.json(data);
    } catch (error) {
        console.error("Error fetching search results:", error);
        return NextResponse.json({ error: "Internal server error" }, { status: 500 });
    }
}