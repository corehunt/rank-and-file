import Image from "next/image";
import Link from "next/link";
import { Card, CardContent } from "@/components/ui/card";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { FileText, Download, Calendar, Building2, Users2, Link as LinkIcon, Tag } from "lucide-react";
import NotFound from "@/app/not-found";
import {getNumberSuffix} from "@/utils/numberUtils";

interface BillDTO {
  billId: string;
  billNo: string;
  billTitle: string;
  introducedDt: string;
  latestActionDt: string;
  latestActionTxt: string;
  policyArea: string;
  congress: number;
  billType: string;
  originChamber: string;
  summaryTxt: string;
  actions: ActionDTO[];
  sponsorships: SponsoredLegislationDTO[];
  billTexts: TextDTO[];
}

interface ActionDTO {
  actionId: string;
  actionCode: string| null;
  actionDate: string| null;
  sourceSystemCode: string| null;
  sourceSystemName: string| null;
  actionText: string| null;
  actionType: string| null;
  committeeRef: string | null;
}

interface PersonSponsorDTO {
  personId: string;
  firstName: string | null;
  midName: string | null;
  lastName: string | null;
  fullName: string;
  state: string | null;
  currentDistrict: number | null;
  imageUrl: string | null;
  partyMembership: string | null;
}

interface SponsoredLegislationDTO {
  sponLegId: string;
  sponsorType: string | null;
  person: PersonSponsorDTO | null;
}

interface TextDTO {
  textId: string;
  versionDate: string;
  versionType: string;
  pdfUrl: string;
}


export default async function BillPage({
                                   params
                                  }: {
  params: { billId: string };
}) {
  const res = await fetch(
      `http://localhost:8080/api/internal/bill/${params.billId}`,
      {
        cache: "no-store",
      }
  );

  if(!res.ok) {
    return NotFound();
  }

  const bill: BillDTO = await res.json();

  const billData = {
    billId: bill.billId,
    billNo: bill.billNo,
    billTitle: bill.billTitle,
    introducedDt: bill.introducedDt,
    latestActionDt: bill.latestActionDt,
    latestActionTxt: bill.latestActionTxt,
    policyArea: bill.policyArea,
    congress: bill.congress,
    billType: bill.billType,
    originChamber: bill.originChamber,
    summaryTxt: bill.summaryTxt,
    actions: bill.actions
        ? bill.actions.map((action) => ({
          actionId: action.actionId,
          actionCode: action.actionCode,
          actionDate: action.actionDate,
          sourceSystemCode: action.sourceSystemCode,
          sourceSystemName: action.sourceSystemName,
          actionText: action.actionText,
          actionType: action.actionType,
          committeeRef: action.committeeRef,
        }))
        : null,
    sponsorships: bill.sponsorships
        ? bill.sponsorships.map((sponsorship) => ({
          sponLegId: sponsorship.sponLegId,
          sponsorType: sponsorship.sponsorType,
          person: sponsorship.person
              ? {
                personId: sponsorship.person.personId,
                firstName: sponsorship.person.firstName || null,
                midName: sponsorship.person.midName || null,
                lastName: sponsorship.person.lastName || null,
                fullName: sponsorship.person.fullName,
                state: sponsorship.person.state || null,
                currentDistrict: sponsorship.person.currentDistrict || null,
                imageUrl: sponsorship.person.imageUrl || null,
                partyMembership: sponsorship.person.partyMembership || null,
              }
              : null,
        }))
        : [],
    billTexts: bill.billTexts
        ? bill.billTexts.map((text) => ({
          textId: text.textId,
          versionDate: text.versionDate,
          versionType: text.versionType,
          pdfUrl: text.pdfUrl,
        }))
        : [],

    // Additional computed fields
    title: bill.billTitle || "Untitled Bill",
    introductionYear: bill.introducedDt
        ? new Date(bill.introducedDt).getFullYear()
        : "N/A",
    lastActionDateFormatted: bill.latestActionDt
        ? new Date(bill.latestActionDt).toLocaleDateString()
        : "N/A",
    shortSummary:
        bill.summaryTxt && bill.summaryTxt.length > 100
            ? bill.summaryTxt.substring(0, 100) + "..."
            : bill.summaryTxt || "No summary available",
  };

  const latestAction = bill.actions && bill.actions.length > 0
      ? [...bill.actions].sort((a, b) => {
        const dateA = a.actionDate ? new Date(a.actionDate).getTime() : 0;
        const dateB = b.actionDate ? new Date(b.actionDate).getTime() : 0;
        return dateB - dateA; // Descending order
      })[0]
      : null;

  const getPartyBadgeVariant = (party: string) => {
    switch (party) {
      case "D":
        return "default";
      case "R":
        return "destructive";
      default:
        return "outline";
    }
  };

  const mainSponsor = bill.sponsorships
      ? bill.sponsorships.find((s) => s.sponsorType === "Sponsor")
      : null;

  function mapPartyCodeToName(partyCode: string | null): string {
    const partyMap: { [key: string]: string } = {
      R: "Republican",
      D: "Democratic",
      I: "Independent",
    };
    return partyMap[partyCode || ""] || "Unknown";
  }


  // @ts-ignore
  return (
      <div className="min-h-screen bg-muted/30">
        <div className="bg-background border-b">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
            <div className="space-y-4">
              <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
                <div>
                  <h1 className="text-2xl sm:text-3xl font-bold">{bill.billType} {bill.billNo} - {bill.billTitle}</h1>
                  <p className="text-base sm:text-lg text-muted-foreground">
                    {bill.originChamber} Bill • {bill.congress}{getNumberSuffix(bill.congress)} Congress
                  </p>
                </div>
                <Badge variant="default">{bill.introducedDt}</Badge>
              </div>
            </div>
          </div>
        </div>

        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <Tabs defaultValue="overview" className="space-y-6">
            <TabsList>
              <TabsTrigger value="overview">Overview</TabsTrigger>
              <TabsTrigger value="text">Full Text</TabsTrigger>
              <TabsTrigger value="actions">Actions</TabsTrigger>
              <TabsTrigger value="amendments">Amendments</TabsTrigger>
              <TabsTrigger value="committees">Committees</TabsTrigger>
              <TabsTrigger value="related">Related Bills</TabsTrigger>
            </TabsList>

            <TabsContent value="overview">
              <div className="grid gap-6 md:grid-cols-3">
                <div className="md:col-span-2 space-y-6">
                  {/* Summary */}
                  <Card>
                    <CardContent className="pt-6">
                      <h2 className="text-xl font-semibold mb-4">Summary</h2>
                      <p className="text-muted-foreground">{bill.summaryTxt}</p>
                    </CardContent>
                  </Card>

                  {/* Subjects */}
                  {/*<Card>*/}
                  {/*  <CardContent className="pt-6">*/}
                  {/*    <h2 className="text-xl font-semibold mb-4">Subjects</h2>*/}
                  {/*    <div className="flex flex-wrap gap-2">*/}
                  {/*      {bill..map((subject, index) => (*/}
                  {/*          <Badge key={index} variant="secondary">*/}
                  {/*            <Tag className="h-3 w-3 mr-1" />*/}
                  {/*            {subject}*/}
                  {/*          </Badge>*/}
                  {/*      ))}*/}
                  {/*    </div>*/}
                  {/*  </CardContent>*/}
                  {/*</Card>*/}

                  {/* Latest Action */}
                  <Card>
                    <CardContent className="pt-6">
                      <h2 className="text-xl font-semibold mb-4">Latest Action</h2>
                      <div className="space-y-2">
                        <p className="text-muted-foreground">{latestAction.actionText}</p>
                        <p className="text-sm text-muted-foreground">
                          {new Date(latestAction?.actionDate).toLocaleDateString()}
                        </p>
                      </div>
                    </CardContent>
                  </Card>
                </div>

                <div className="space-y-6">
                  {/* Bill Information */}
                  <Card>
                    <CardContent className="pt-6">
                      <h2 className="text-xl font-semibold mb-4">Bill Information</h2>
                      <div className="space-y-4">
                        <div>
                          <p className="text-sm font-medium">Introduced</p>
                          <p className="text-muted-foreground">
                            {new Date(bill.introducedDt).toLocaleDateString()}
                          </p>
                        </div>
                        <div>
                          <p className="text-sm font-medium">Origin Chamber</p>
                          <p className="text-muted-foreground">{bill.originChamber}</p>
                        </div>
                        <div>
                          <p className="text-sm font-medium">Policy Area</p>
                          <p className="text-muted-foreground">{bill.policyArea}</p>
                        </div>
                      </div>
                    </CardContent>
                  </Card>

                  {/* Sponsor */}
                  <Card>
                    <CardContent className="pt-6">
                      <h2 className="text-xl font-semibold mb-4">Sponsor</h2>
                      {mainSponsor && mainSponsor.person ? (
                          <div className="flex items-center gap-4">
                            <div className="relative h-16 w-16 rounded-lg overflow-hidden">
                              <Image
                                  src={mainSponsor.person.imageUrl || "/placeholder.jpg"} // Fallback if no image
                                  alt={mainSponsor.person.fullName}
                                  fill
                                  className="object-cover"
                              />
                            </div>
                            <div>
                              <Link
                                  href={`/politicians/${mainSponsor.person.personId
                                      .toLowerCase()
                                      .replace(/\s+/g, "-")}`}
                                  className="font-medium hover:text-primary"
                              >
                                {mainSponsor.person.fullName}
                              </Link>
                              <div className="flex items-center gap-2 mt-1">
                                <Badge variant={getPartyBadgeVariant(mainSponsor.person.partyMembership)}>
                                  {mapPartyCodeToName(mainSponsor.person.partyMembership)}
                                </Badge>
                                <span className="text-sm text-muted-foreground">
                                    {mainSponsor.person.state}-{mainSponsor.person.currentDistrict}{getNumberSuffix(mainSponsor.person.currentDistrict)}
                                </span>
                              </div>
                            </div>
                          </div>
                      ) : (
                          <p className="text-muted-foreground">No sponsor found</p>
                      )}
                    </CardContent>
                  </Card>

                  {/* Co-Sponsors */}
                  <Card>
                    <CardContent className="pt-6">
                      <h2 className="text-xl font-semibold mb-4">
                        Co-Sponsors ({bill.sponsorships.length})
                      </h2>
                      <div className="space-y-4">
                        {bill.sponsorships.map((cosponsor, index) => (
                            <div key={index} className="flex items-center justify-between">
                              <Link
                                  href={`/politicians/${cosponsor.person?.personId}`}
                                  className="hover:text-primary"
                              >
                                {cosponsor.person?.fullName}
                              </Link>
                              <div className="flex items-center gap-2">
                                <span className="text-sm text-muted-foreground">
                              {cosponsor.person?.state}-{cosponsor.person?.currentDistrict}
                                </span>
                                <Badge variant={getPartyBadgeVariant(cosponsor.person?.partyMembership)}>
                                  {cosponsor.person?.partyMembership}
                                </Badge>
                              </div>
                            </div>
                        ))}
                      </div>
                    </CardContent>
                  </Card>
                </div>
              </div>
            </TabsContent>

            <TabsContent value="text">
              <Card>
                <CardContent className="pt-6">
                  <h2 className="text-xl font-semibold mb-4">Bill Texts</h2>
                  <div className="space-y-4">
                    {bill.billTexts.map((text, index) => (
                        <div key={index} className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b pb-4 last:border-0 last:pb-0">
                          <div>
                            <p className="font-medium">{text.versionType}</p>
                            <p className="text-sm text-muted-foreground">
                              {new Date(text.versionDate).toLocaleDateString()}
                              {/*• {text.pages} pages*/}
                            </p>
                          </div>
                          <Button variant="outline" asChild>
                            <Link href={text.pdfUrl}>
                              <Download className="h-4 w-4 mr-2" />
                              Download PDF
                            </Link>
                          </Button>
                        </div>
                    ))}
                  </div>
                </CardContent>
              </Card>
            </TabsContent>

            <TabsContent value="actions">
              <Card>
                <CardContent className="pt-6">
                  <h2 className="text-xl font-semibold mb-4">Legislative Actions</h2>
                  <div className="space-y-4">
                    {bill.actions.map((action, index) => (
                        <div key={index} className="flex flex-col sm:flex-row justify-between gap-2 border-b pb-4 last:border-0 last:pb-0">
                          <div className="space-y-1">
                            <p className="text-muted-foreground">{action.actionText}</p>
                            <Badge variant="outline">{action.actionType}</Badge>
                          </div>
                          <p className="text-sm text-muted-foreground shrink-0">
                            {new Date(action.actionDate).toLocaleDateString()}
                          </p>
                        </div>
                    ))}
                  </div>
                </CardContent>
              </Card>
            </TabsContent>

            <TabsContent value="amendments">
              {/*<Card>*/}
              {/*  <CardContent className="pt-6">*/}
              {/*    <h2 className="text-xl font-semibold mb-4">Amendments</h2>*/}
              {/*    <div className="space-y-6">*/}
              {/*      {bill.amendments.map((amendment, index) => (*/}
              {/*          <div key={index} className="border-b pb-6 last:border-0 last:pb-0">*/}
              {/*            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-4">*/}
              {/*              <div>*/}
              {/*                <p className="font-medium">{amendment.number}</p>*/}
              {/*                <p className="text-sm text-muted-foreground">*/}
              {/*                  Sponsored by {amendment.sponsor}*/}
              {/*                </p>*/}
              {/*              </div>*/}
              {/*              <Badge variant={amendment.status === "Agreed to" ? "default" : "secondary"}>*/}
              {/*                {amendment.status}*/}
              {/*              </Badge>*/}
              {/*            </div>*/}
              {/*            <p className="text-muted-foreground">{amendment.purpose}</p>*/}
              {/*            <p className="text-sm text-muted-foreground mt-2">*/}
              {/*              {new Date(amendment.date).toLocaleDateString()}*/}
              {/*            </p>*/}
              {/*          </div>*/}
              {/*      ))}*/}
              {/*    </div>*/}
              {/*  </CardContent>*/}
              {/*</Card>*/}
            </TabsContent>

            <TabsContent value="committees">
              {/*<Card>*/}
              {/*  <CardContent className="pt-6">*/}
              {/*    <h2 className="text-xl font-semibold mb-4">Committee Assignments</h2>*/}
              {/*    <div className="space-y-6">*/}
              {/*      {bill.committees.map((committee, index) => (*/}
              {/*          <div key={index} className="flex flex-col sm:flex-row justify-between gap-4 border-b pb-6 last:border-0 last:pb-0">*/}
              {/*            <div className="space-y-2">*/}
              {/*              <Link*/}
              {/*                  href={`/committees/${committee.name.toLowerCase().replace(/\s+/g, "-")}`}*/}
              {/*                  className="font-medium hover:text-primary"*/}
              {/*              >*/}
              {/*                {committee.name}*/}
              {/*              </Link>*/}
              {/*              <div className="flex flex-wrap gap-2">*/}
              {/*                <Badge variant="outline">{committee.chamber}</Badge>*/}
              {/*                <Badge variant="secondary">{committee.role}</Badge>*/}
              {/*              </div>*/}
              {/*            </div>*/}
              {/*            <p className="text-sm text-muted-foreground shrink-0">*/}
              {/*              Referred on {new Date(committee.referralDate).toLocaleDateString()}*/}
              {/*            </p>*/}
              {/*          </div>*/}
              {/*      ))}*/}
              {/*    </div>*/}
              {/*  </CardContent>*/}
              {/*</Card>*/}
            </TabsContent>

            <TabsContent value="related">
              {/*<Card>*/}
              {/*  <CardContent className="pt-6">*/}
              {/*    <h2 className="text-xl font-semibold mb-4">Related Bills</h2>*/}
              {/*    <div className="space-y-6">*/}
              {/*      {bill.relatedBills.map((relatedBill, index) => (*/}
              {/*          <div key={index} className="flex flex-col sm:flex-row justify-between gap-4 border-b pb-6 last:border-0 last:pb-0">*/}
              {/*            <div className="space-y-2">*/}
              {/*              <Link*/}
              {/*                  href={`/bills/${relatedBill.number.toLowerCase().replace(/\s+/g, "-")}`}*/}
              {/*                  className="font-medium hover:text-primary"*/}
              {/*              >*/}
              {/*                {relatedBill.number} - {relatedBill.title}*/}
              {/*              </Link>*/}
              {/*              <div className="flex flex-wrap gap-2">*/}
              {/*                <Badge variant="outline">{relatedBill.congress}</Badge>*/}
              {/*                <Badge variant="secondary">{relatedBill.relationship}</Badge>*/}
              {/*              </div>*/}
              {/*            </div>*/}
              {/*          </div>*/}
              {/*      ))}*/}
              {/*    </div>*/}
              {/*  </CardContent>*/}
              {/*</Card>*/}
            </TabsContent>
          </Tabs>
        </div>
      </div>
  );
}