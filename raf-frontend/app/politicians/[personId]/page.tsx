import Image from "next/image";
import { notFound } from "next/navigation";
import { Card, CardContent } from "@/components/ui/card";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Badge } from "@/components/ui/badge";
import { Phone, Mail, MapPin, Globe } from "lucide-react";
import Link from "next/link";

interface TermDTO {
  termId: number;
  chamber: string;
  congress: number;
  district: number | null;
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

export default async function PoliticianProfile({
                                                  params,
                                                }: {
  params: { personId: string };
}) {
  const res = await fetch(
      `http://localhost:8080/api/internal/politician/${params.personId}`,
      {
        cache: "no-store",
      }
  );

  if (!res.ok) {
    return notFound();
  }

  const politician: PersonDTO = await res.json();

  // Map the fields from PersonDTO to match your component's data structure
  const politicianData = {
    personId: politician.personId,
    firstName: politician.firstName,
    midName: politician.midName,
    lastName: politician.lastName,
    fullName: politician.fullName,
    birthDate: politician.birthDate,
    deathDate: politician.deathDate,
    website: politician.website,
    officeLocLine1: politician.officeLocLine1,
    officeLocLine2: politician.officeLocLine2,
    phoneNo: politician.phoneNo,
    state: politician.state,
    currentDistrict: politician.currentDistrict,
    currentMember: politician.currentMember,
    biography: politician.biography,
    email: politician.email,
    imageUrl: politician.imageUrl,
    imgAttribution: politician.imgAttribution,
    partyMembership: politician.partyMembership,
    partyStartYr: politician.partyStartYr,
    termList: politician.termList,
    // Additional computed fields for your UI
    name:
        politician.fullName ||
        `${politician.firstName || ""} ${politician.lastName || ""}`.trim(),
    district: politician.currentDistrict
        ? `District ${politician.currentDistrict}`
        : "At Large",
    chamber: getCurrentChamber(politician.termList),
    membershipStatus:
        politician.currentMember === "Yes" ? "Incumbent" : "Former Member",
    yearsActive: getYearsActive(politician.termList),
    officeLocation: {
      dc: politician.officeLocLine1 || "N/A",
      district: politician.officeLocLine2 || "N/A",
    },
    phone: {
      dc: politician.phoneNo || "N/A"
    },
    congressionalRecord: {
      sponsoredBills: [], // Populate if available
      recentVotes: [], // Populate if available
      committees: [], // Populate if available
    },
  };

  return (
      <div className="min-h-screen bg-muted/30">
        <div className="bg-background border-b">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
            <div className="flex flex-col md:flex-row gap-6">
              <div className="relative h-48 w-48 rounded-lg overflow-hidden flex-shrink-0 mx-auto md:mx-0">
                <Image
                    src={politicianData.imageUrl || "/default-image.jpg"}
                    alt={politicianData.name}
                    fill
                    className="object-cover"
                />
              </div>
              <div className="flex-grow">
                <div className="grid md:grid-cols-3 gap-6">
                  {/* First Column: Basic Info */}
                  <div className="space-y-2 text-center md:text-left">
                    <h1 className="text-3xl font-bold">{politicianData.name}</h1>
                    <p className="text-lg text-muted-foreground">
                      {politicianData.district}, {politicianData.state || "N/A"}
                    </p>
                    <div className="flex flex-wrap gap-2 justify-center md:justify-start">
                      <Badge variant={politicianData.partyMembership === "D" ? "default" : "destructive"}>
                        {mapPartyCodeToName(politicianData.partyMembership)}
                      </Badge>
                      <Badge variant="outline">
                        {politicianData.membershipStatus}
                      </Badge>
                    </div>
                  </div>

                  {/* Second Column: Chamber & Years */}
                  <div className="grid grid-cols-2 md:block text-center md:text-left gap-4">
                    <div>
                      <p className="text-sm font-medium">Chamber</p>
                      <p className="text-muted-foreground">
                        {politicianData.chamber}
                      </p>
                    </div>
                    <div>
                      <p className="text-sm font-medium">Years Active</p>
                      <p className="text-muted-foreground">
                        {politicianData.yearsActive}
                      </p>
                    </div>
                  </div>

                  {/* Third Column: Contact Info */}
                  <div className="space-y-3 text-left">
                    <div className="flex items-start gap-2">
                      <MapPin className="h-5 w-5 mt-0.5 text-muted-foreground" />
                      <div className="text-sm">
                        <p className="font-medium">Offices</p>
                        <p className="text-muted-foreground">
                          {politicianData.officeLocation.dc}
                        </p>
                        <p className="text-muted-foreground">
                          {politicianData.officeLocation.district}
                        </p>
                      </div>
                    </div>
                    <div className="flex items-start gap-2">
                      <Phone className="h-5 w-5 mt-0.5 text-muted-foreground" />
                      <div className="text-sm">
                        <p className="font-medium">Phone</p>
                        <p className="text-muted-foreground">
                          {politicianData.phone.dc}
                        </p>
                      </div>
                    </div>
                    {politicianData.email && (
                        <div className="flex items-start gap-2">
                          <Mail className="h-5 w-5 mt-0.5 text-muted-foreground" />
                          <div className="text-sm">
                            <p className="font-medium">Email</p>
                            <p className="text-muted-foreground">
                              {politicianData.email}
                            </p>
                          </div>
                        </div>
                    )}
                    {politicianData.website && (
                        <div className="flex items-start gap-2">
                          <Globe className="h-5 w-5 mt-0.5 text-muted-foreground"/>
                          <div className="text-sm">
                            <p className="font-medium">Website</p>
                            <Link
                                href={politician.website}
                                target="_blank"
                                rel="noopener noreferrer"
                                className="text-muted-foreground hover:text-primary transition-colors"
                            >
                              {politician.website.replace('https://', '')}
                            </Link>
                          </div>
                        </div>
                    )}
                  </div>
                </div>

                {/* Biography Row */}
                {politicianData.biography && (
                    <div className="mt-6">
                      <p className="text-muted-foreground md:text-left">
                        {politicianData.biography}
                      </p>
                    </div>
                )}
              </div>
            </div>
          </div>
        </div>

        {/* Tabs for Additional Information */}
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <Tabs defaultValue="record" className="space-y-6">
            <TabsList>
              <TabsTrigger value="record">Congressional Record</TabsTrigger>
              <TabsTrigger value="votes">Voting History</TabsTrigger>
              <TabsTrigger value="bills">Sponsored Bills</TabsTrigger>
              <TabsTrigger value="finances">Financial Activity</TabsTrigger>
            </TabsList>

            <TabsContent value="record">
              {/* Display committees, if available */}
              {politicianData.congressionalRecord.committees.length > 0 && (
                  <Card>
                    <CardContent className="pt-6">
                      <h2 className="text-xl font-semibold mb-4">
                        Committee Memberships
                      </h2>
                      <div className="space-y-4">
                        {politicianData.congressionalRecord.committees.map(
                            (committee, index) => (
                                <div
                                    key={index}
                                    className="flex justify-between items-center border-b pb-4 last:border-0 last:pb-0"
                                >
                                  <div>
                                    {/* Uncomment and populate when data is available */}
                                    {/* <p className="font-medium">{committee.name}</p>
                            <p className="text-sm text-muted-foreground">{committee.role}</p> */}
                                  </div>
                                </div>
                            )
                        )}
                      </div>
                    </CardContent>
                  </Card>
              )}

              {/* Additional sections for sponsored bills and recent votes can be added similarly */}
            </TabsContent>

            {/* Other Tabs Content */}
            {/* ... */}
          </Tabs>
        </div>
      </div>
  );
}

// Helper function to compute years active
function getYearsActive(termList: TermDTO[] | null): string {
  if (!termList || termList.length === 0) return "N/A";
  const startYears = termList.map((term) => term.startYr);
  const endYears = termList.map((term) => term.endYr ?? new Date().getFullYear());
  const minYear = Math.min(...startYears);
  const maxYear = Math.max(...endYears);
  return `${minYear} - ${maxYear}`;
}

// Helper function to get current chamber
function getCurrentChamber(termList: TermDTO[] | null): string {
  if (!termList || termList.length === 0) return "N/A";
  // Sort terms by endYr descending
  const sortedTerms = [...termList].sort((a, b) => {
    const aEndYear = a.endYr ?? new Date().getFullYear();
    const bEndYear = b.endYr ?? new Date().getFullYear();
    return bEndYear - aEndYear;
  });
  const latestTerm = sortedTerms[0];
  return latestTerm.chamber || "N/A";
}

// Helper function to map party code to full name
function mapPartyCodeToName(partyCode: string | null): string {
  const partyMap: { [key: string]: string } = {
    R: "Republican",
    D: "Democratic",
    I: "Independent",
  };
  return partyMap[partyCode || ""] || "Unknown";
}