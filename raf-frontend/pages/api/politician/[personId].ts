import { NextApiRequest, NextApiResponse } from 'next';

interface Person {
    personId: string;
    firstName: string;
    midName?: string;
    lastName: string;
    birthDate?: string;
    deathDate?: string;
    website?: string;
    officeLocLine1?: string;
    officeLocLine2?: string;
    phoneNo?: string;
    state?: string;
    currentDistrict?: number;
    biography?: string;
    email?: string;
    imageUrl?: string;
    imgAttribution?: string;
    partyMembership?: string;
    partyStartYr?: number;
}

const fetchPersonById = async (personId: string): Promise<Person> => {
    const response = await fetch(`http://localhost:8080/api/internal/politician/${personId}`);
    if (!response.ok) {
        const errorText = await response.text();  // Capture the error response text for logging
        console.error(`Failed to fetch politician: ${response.status} ${response.statusText} ${errorText}`);
        throw new Error(`Failed to fetch politician: ${response.status} ${response.statusText}`);
    }
    return response.json();
};

const handler = async (req: NextApiRequest, res: NextApiResponse) => {
    const { personId } = req.query;

    try {
        if (!personId || Array.isArray(personId)) {
            res.status(400).json({ error: 'Invalid personId' });
            return;
        }

        const person = await fetchPersonById(personId);
        res.status(200).json(person);
    } catch (error: any) {
        console.error('Error fetching politician:', error);
        res.status(500).json({ error: error.message });
    }
};

export default handler;
