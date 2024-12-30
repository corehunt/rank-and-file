import { NextRequest, NextResponse } from "next/server";

export const revalidate = 0;

interface TermDTO {
    termId: number;
    chamber: string;
    congress: number;
    district: number;
    startYr: number;
    endYr: number | null;
    memberType: string;
    stateCd: string;
    stateNm: string;
}

interface PersonDTO {
    personId: string;
    firstName: string | null;
    midName: string | null;
    lastName: string | null;
    fullName: string | null;
    birthDate: string | null;
    deathDate: string | null;
    website: string | null;
    officeLocLine1: string | null;
    officeLocLine2: string | null;
    phoneNo: string | null;
    state: string | null;
    currentDistrict: number | null;
    currentMember: string | null;
    biography: string | null;
    email: string | null;
    imageUrl: string | null;
    imgAttribution: string | null;
    partyMembership: string | null;
    partyStartYr: number | null;
    termList: TermDTO[] | null;
}

interface PageDTO<T> {
    content: T[];
    totalPages: number;
    totalElements: number;
    size: number;
    number: number; // zero-based page index
}

export async function GET(request: NextRequest) {
    try {
        // Extract all query params from the request URL
        const { searchParams } = new URL(request.url);
        const searchQuery = searchParams.get("q") || "";
        const chamber = searchParams.get("chamber") || "";
        const party = searchParams.get("party") || "";
        const status = searchParams.get("status") || "";
        const page = parseInt(searchParams.get("page") || "0", 10);
        const size = parseInt(searchParams.get("size") || "20", 10);

        // Build the backend URL with query parameters
        const backendUrl = new URL(`http://localhost:8080/api/internal/politicians/search`);
        if (searchQuery) {
            backendUrl.searchParams.append("q", searchQuery);
        }
        if (chamber) {
            backendUrl.searchParams.append("chamber", chamber);
        }
        if (party) {
            backendUrl.searchParams.append("party", party);
        }
        if (status) {
            backendUrl.searchParams.append("status", status);
        }
        backendUrl.searchParams.append("page", String(page));
        backendUrl.searchParams.append("size", String(size));

        console.log(`Fetching politicians (search) from: ${backendUrl.toString()}`);

        const response = await fetch(backendUrl.toString(), {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
            },
        });

        if (!response.ok) {
            const errorText = await response.text();
            console.error("Error from backend search:", errorText);
            return NextResponse.json(
                { error: "Failed to fetch search results" },
                { status: response.status }
            );
        }

        const data: PageDTO<PersonDTO> = await response.json();
        return NextResponse.json(data);
    } catch (error) {
        console.error("Error fetching search results:", error);
        return NextResponse.json(
            { error: "Internal server error" },
            { status: 500 }
        );
    }
}
