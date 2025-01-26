import { NextRequest, NextResponse } from 'next/server';

export async function GET(
    req: NextRequest,
    { params }: { params: { personId: string } }
) {
    const { personId } = params;
    const { searchParams } = new URL(req.url);
    const page = searchParams.get('page') || '0'; // Backend expects 0-based indexing
    const size = searchParams.get('size') || '10';

    try {
        const response = await fetch(
            `${process.env.BACKEND_BASE_URL}/api/internal/politician/${personId}/cosponsored?page=${page}&size=${size}`,
            {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                },
                cache: 'no-store', // Disable caching
            }
        );

        if (!response.ok) {
            return NextResponse.json(
                { message: 'Error fetching data' },
                { status: response.status }
            );
        }

        const data = await response.json();

        return NextResponse.json(data);
    } catch (error) {
        console.error('Error fetching co-sponsored legislation:', error);
        return NextResponse.json(
            { message: 'Internal Server Error' },
            { status: 500 }
        );
    }
}
