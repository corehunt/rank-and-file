import { NextResponse } from 'next/server';

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

export async function GET(
    request: Request,
    { params }: { params: { searchTerm: string } }
) {
    const { searchTerm } = params;

    if (!searchTerm) {
        return NextResponse.json({ error: 'Invalid search term' }, { status: 400 });
    }

    try {
        const backendUrl = `http://localhost:8080/api/internal/${encodeURIComponent(searchTerm)}`;

        console.log(`Fetching data from backend API: ${backendUrl}`);

        const response = await fetch(backendUrl);

        if (!response.ok) {
            console.error(`Backend API responded with status ${response.status}`);
            const errorText = await response.text();
            console.error(`Backend API error response: ${errorText}`);
            return NextResponse.json(
                { error: 'Failed to fetch politicians from backend API' },
                { status: response.status }
            );
        }

        const politicians: PersonDTO[] = await response.json();

        console.log('Received data from backend API:', politicians);

        return NextResponse.json(politicians);
    } catch (error) {
        console.error('Error fetching politicians:', error);
        return NextResponse.json(
            { error: 'Internal server error' },
            { status: 500 }
        );
    }
}
