import { NextApiRequest, NextApiResponse } from 'next';

interface BillDTO {
    billId: string;
    billNo: number;
    billTitle: string;
    introducedDt: string;
    latestActionDt: string;
    latestActionTxt: string;
    policyArea: string;
    congress: number;
    billType: string;
    originChamber: string;
    summaryTxt: string;
}

interface SponsoredLegislationDTO {
    sponLegId: string;
    sponsorType: string;
    bill: BillDTO;
}

interface PaginatedData<T> {
    content: T[];
    totalPages: number;
    totalElements: number;
    size: number;
    number: number;
    numberOfElements: number;
    first: boolean;
    last: boolean;
}

const fetchSponsoredLegislation = async (
    personId: string,
    page: number,
    size: number
): Promise<PaginatedData<SponsoredLegislationDTO>> => {
    const response = await fetch(
        `http://localhost:8080/api/internal/politician/${personId}/sponsored?page=${page}&size=${size}`
    );
    if (!response.ok) {
        const errorText = await response.text();
        console.error(`Failed to fetch sponsored legislation: ${response.status} ${response.statusText} ${errorText}`);
        throw new Error(`Failed to fetch sponsored legislation: ${response.status} ${response.statusText}`);
    }
    return response.json();
};

export default async (req: NextApiRequest, res: NextApiResponse) => {
    const { personId } = req.query;
    const { page = '0', size = '10' } = req.query;

    try {
        if (!personId || Array.isArray(personId)) {
            res.status(400).json({ error: 'Invalid personId' });
            return;
        }

        const data = await fetchSponsoredLegislation(
            personId as string,
            parseInt(page as string, 10),
            parseInt(size as string, 10)
        );
        res.status(200).json(data);
    } catch (error: any) {
        console.error('Error fetching sponsored legislation:', error);
        res.status(500).json({ error: error.message });
    }
};
