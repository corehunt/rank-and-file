import { NextResponse } from "next/server";

export const revalidate = 0;

interface ActionDTO {
    actionId: string;
    actionCode: string | null;
    actionDate: string | null;
    sourceSystemCode: string | null;
    sourceSystemName: string | null;
    actionText: string | null;
    actionType: string | null;
    committeeRef: string | null;
}

interface SponsoredLegPersonDTO {
    sponLegId: string;
    sponsorType: string | null;
    person: PersonSummaryDTO | null;
}

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

interface TextDTO {
    textId: string;
    versionDate: string;
    versionType: string;
    pdfUrl: string;
}

interface RelatedBillDTO {
    billId: string;
    billNo: number;
    billTitle: string;
    introducedDt: string;
    congress: number;
    billType: string;
    originChamber: string;
}

interface CommitteeDTO {
    committeeId: string;
    chamber: string;
    commTypeCd: string;
    commName: string;
    sysCode: string;
}

interface BillDTO {
    billId: string;
    billNo: number | null;
    billTitle: string | null;
    introducedDt: string | null;
    latestActionDt: string | null;
    latestActionTxt: string | null;
    policyArea: string | null;
    congress: number | null;
    billType: string | null;
    originChamber: string | null;
    summaryTxt: string | null;
    legislativeSubjects: string | null;
    actions: ActionDTO[] | null;
    sponsorships: SponsoredLegPersonDTO[] | null;
    billTexts: TextDTO[] | null;
    relatedBills: RelatedBillDTO[] | null;
    committees: CommitteeDTO[] | null;
}

/**
 * GET /api/bills/recent
 * Fetches the recent bills from backend API.
 */
export async function GET() {
    try {
        // Adjust this to your actual endpoint:
        const backendUrl = `${process.env.BACKEND_BASE_URL}/api/internal/bill/recent`;
        console.log(`Fetching recent bills data from backend API: ${backendUrl}`);

        const response = await fetch(backendUrl);

        if (!response.ok) {
            console.error(`Backend API responded with status ${response.status}`);
            const errorText = await response.text();
            console.error(`Backend API error response: ${errorText}`);
            return NextResponse.json(
                { error: "Failed to fetch recent bills from backend API" },
                { status: response.status }
            );
        }

        const recentBills: BillDTO[] = await response.json();
        console.log("Received recent bills data from backend API:", recentBills);

        // Return data as JSON
        return NextResponse.json(recentBills);
    } catch (error) {
        console.error("Error fetching recent bills:", error);
        return NextResponse.json({ error: "Internal server error" }, { status: 500 });
    }
}
