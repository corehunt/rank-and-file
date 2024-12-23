import { NextResponse } from 'next/server';

export const revalidate = 0;

interface PersonSummaryDTO {
    personId: string;
    firstName: string | null;
    midName: string | null;
    lastName: string | null;
    fullName: string | null;
    state: string | null;
    currentDistrict: number | null;
    imageUrl: string | null;
    partyMembership: string | null;
}

interface LeadershipDTO {
    leadershipId: string;
    leadershipType: string;
    currentLeader: string;
    person: PersonSummaryDTO;
}

export async function GET() {
    try {
        const backendUrl = 'http://localhost:8080/api/internal/politician/leadership';
        console.log(`Fetching leadership data from backend API: ${backendUrl}`);

        const response = await fetch(backendUrl);

        if (!response.ok) {
            console.error(`Backend API responded with status ${response.status}`);
            const errorText = await response.text();
            console.error(`Backend API error response: ${errorText}`);
            return NextResponse.json(
                { error: 'Failed to fetch leadership data from backend API' },
                { status: response.status }
            );
        }

        const leadership: LeadershipDTO[] = await response.json();
        console.log('Received leadership data from backend API:', leadership);

        return NextResponse.json(leadership);
    } catch (error) {
        console.error('Error fetching leadership:', error);
        return NextResponse.json({ error: 'Internal server error' }, { status: 500 });
    }
}
