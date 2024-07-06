import { NextApiRequest, NextApiResponse } from 'next';

interface Politician {
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

const fetchPoliticians = async (searchTerm: string): Promise<Politician[]> => {
    const response = await fetch(`http://localhost:8080/api/internal/${searchTerm}`);
    if (!response.ok) {
        throw new Error('Failed to fetch politicians');
    }
    return response.json();
};

export default async (req: NextApiRequest, res: NextApiResponse) => {
    const { searchTerm } = req.query;

    try {
        if (!searchTerm || Array.isArray(searchTerm)) {
            throw new Error('Invalid search term');
        }

        const politicians = await fetchPoliticians(searchTerm as string);
        res.status(200).json(politicians);
    } catch (error: any) {
        console.error('Error fetching politicians:', error);
        res.status(500).json({ error: error.message });
    }
};
