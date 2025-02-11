import Image from "next/image";
import { notFound } from "next/navigation";
import { Card, CardContent } from "@/components/ui/card";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Badge } from "@/components/ui/badge";
import { Phone, Mail, MapPin, Globe } from "lucide-react";
import Link from "next/link";
import backupImg from "@/app/assets/backup.png"

import SponsoredBills from "@/components/politicians/SponsoredBills";
import CoSponsoredBills from "@/components/politicians/CoSponsoredBills";
import {getNumberSuffix} from "@/utils/numberUtils";

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

const atLargeStates = [
  "Alaska",
  "Wyoming",
  "Montana",
  "North Dakota",
  "South Dakota",
  "Vermont",
  "Delaware",
  "Virgin Islands",
  "Puerto Rico",
  "District of Columbia",
  "Guam",
  "American Samoa",
  "Northern Mariana Islands",
];

const getMostRecentTerm = (termList: TermDTO[] | null): TermDTO | undefined => {
  if (!termList || termList.length === 0) return undefined;
  return termList.reduce((prev, current) =>
      current.congress > prev.congress ? current : prev
  );
};

/**
 * Determines the district display string based on the most recent term.
 * @param term The most recent TermDTO object.
 * @returns A string representing the district or "At Large".
 */
const getDistrictDisplay = (term: TermDTO | undefined): string => {
  if (!term || term.chamber === "Senate") return "";

  if (atLargeStates.includes(term.stateNm)) {
    return "At Large";
  }

  return term.district ? `District ${term.district}` : "District Unknown";
};

export default async function PoliticianProfile({
                                                  params,
                                                }: {
  params: { personId: string };
}) {
  const res = await fetch(
      `${process.env.BACKEND_BASE_URL}/api/internal/politician/${params.personId}`,
      {
        cache: "no-store",
      }
  );

  const backendUrl = `${process.env.BACKEND_BASE_URL}/api/internal/politician/${params.personId}`;
  console.log(`getting person with personId: ${backendUrl}`);


  if (!res.ok) {
    return notFound();
  }

  const politician: PersonDTO = await res.json();

  const mostRecentTerm = getMostRecentTerm(politician.termList);

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
    name:
        politician.fullName ||
        `${politician.firstName || ""} ${politician.lastName || ""}`.trim(),
    district: getDistrictDisplay(mostRecentTerm),
    chamber: mostRecentTerm?.chamber || getCurrentChamber(politician.termList),
    membershipStatus:
        politician.currentMember === "Yes" ? "Incumbent" : "Former Member",
    yearsActive: getYearsActive(politician.termList),
    officeLocation: {
      dc: politician.officeLocLine1 || "N/A",
      district: politician.officeLocLine2 || "N/A",
    },
    phone: {
      dc: politician.phoneNo || "N/A",
    },
  };

  return (
      <div className="min-h-screen bg-muted/30">
        <div className="bg-background border-b">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
            <div className="flex flex-col md:flex-row gap-6">
              <div className="relative h-48 w-48 rounded-lg overflow-hidden flex-shrink-0 mx-auto md:mx-0">
                <Image
                    src={politicianData.imageUrl || backupImg}
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
                      {politicianData.chamber === "Senate"
                          ? `Senator ${politicianData.state}`
                          : politicianData.district && politicianData.state
                              ? `${politicianData.district}, ${politicianData.state}`
                              : "Information not available"}
                    </p>
                    <div className="flex flex-wrap gap-2 justify-center md:justify-start">
                      <Badge
                          variant={
                            politicianData.partyMembership === "D"
                                ? "default" // Blue
                                : politicianData.partyMembership === "R"
                                    ? "destructive" // Red
                                    : politicianData.partyMembership === "I"
                                        ? "success" // Green
                                        : "outline" // Default or unknown
                          }
                          aria-label={`Party: ${politicianData.partyMembership}`}
                      >
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
                          <Globe className="h-5 w-5 mt-0.5 text-muted-foreground" />
                          <div className="text-sm">
                            <p className="font-medium">Website</p>
                            {politician?.website ? (
                                <Link
                                    href={politician.website}
                                    target="_blank"
                                    rel="noopener noreferrer"
                                    className="text-muted-foreground hover:text-primary transition-colors"
                                >
                                  {politician.website.replace("https://", "")}
                                </Link>
                            ) : null}
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
            <TabsList className="flex w-auto md:w-fit overflow-x-auto justify-start">
              <TabsTrigger value="record">Congressional Record</TabsTrigger>
              <TabsTrigger value="sponsored">Sponsored Bills</TabsTrigger>
              <TabsTrigger value="cosponsored">Co-Sponsored Bills</TabsTrigger>
              <TabsTrigger value="finances">Financial Activity</TabsTrigger>
            </TabsList>

            {/* Congressional Record Tab */}
            <TabsContent value="record">
              <Card>
                <CardContent className="pt-6">
                  <h2 className="text-xl font-semibold mb-4">
                    Congressional Terms
                  </h2>
                  <div className="space-y-6">
                    {politician.termList
                        ?.sort((a, b) => b.congress - a.congress)
                        .map((term, index) => (
                            <div
                            key={index}
                            className="border-b last:border-0 pb-6 last:pb-0"
                        >
                          <div className="flex flex-wrap justify-between items-start gap-4">
                            <div>
                              <h3 className="font-medium text-lg">
                                {term.congress}{getNumberSuffix(term.congress)} Congress ({term.startYr}-
                                {term.endYr || "Present"})
                              </h3>
                              <p className="text-muted-foreground">
                                {term.memberType}, {term.stateNm}{" "}
                                {term.district
                                    ? `${term.district}${getNumberSuffix(term.district)} District`
                                    : ""}
                              </p>
                            </div>
                            <Badge variant="outline">{term.chamber}</Badge>
                          </div>
                        </div>
                    ))}
                  </div>
                </CardContent>
              </Card>
            </TabsContent>

            {/* Sponsored Bills Tab */}
            <TabsContent value="sponsored">
              <SponsoredBills personId={politician.personId} />
            </TabsContent>

            {/* Co-Sponsored Bills Tab */}
            <TabsContent value="cosponsored">
              <CoSponsoredBills personId={politician.personId} />
            </TabsContent>

            {/* Financial Activity Tab (Placeholder) */}
            <TabsContent value="finances">
              <p className="text-muted-foreground">
                Financial activity information is not available at this time.
              </p>
            </TabsContent>
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