import Image from "next/image";
import Link from "next/link";
import { Card, CardContent } from "@/components/ui/card";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { FileText, Download, Calendar, Building2, Users2, Link as LinkIcon, Tag } from "lucide-react";

// Make sure generateStaticParams is properly exported
export async function generateStaticParams() {
  // In production, this would fetch from your API/database
  return [
    { slug: "h-r-1234-clean-energy-act" },
    { slug: "s-789-infrastructure-investment-act" }
  ];
}

const getPartyBadgeVariant = (party: string) => {
  switch (party) {
    case "Democratic":
      return "default";
    case "Republican":
      return "destructive";
    default:
      return "outline";
  }
};

export default function BillPage({ params }: { params: { slug: string } }) {
  // Rest of the component code remains exactly the same
  const bill = {
    billNumber: "H.R. 1234",
    title: "Clean Energy Act",
    type: "House Bill",
    congress: "117th Congress",
    introducedDate: "2023-09-15",
    originChamber: "House of Representatives",
    status: "In Committee",
    latestAction: {
      date: "2023-09-20",
      description: "Referred to House Committee on Energy and Commerce"
    },
    summary: "A comprehensive bill to promote renewable energy development and reduce carbon emissions through federal incentives and regulations. The legislation aims to accelerate the transition to clean energy sources, create green jobs, and establish new environmental protection standards.",
    policyArea: "Energy and Environment",
    subjects: [
      "Environmental Protection",
      "Renewable Energy",
      "Climate Change",
      "Infrastructure",
      "Economic Development"
    ],
    sponsor: {
      name: "Jane Smith",
      party: "Democratic",
      state: "CA",
      district: "12th",
      imageUrl: "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?auto=format&fit=crop&q=80&w=200&h=200"
    },
    cosponsors: [
      { name: "John Doe", party: "Republican", state: "TX", district: "7th" },
      { name: "Sarah Johnson", party: "Democratic", state: "NY", district: "4th" }
    ],
    committees: [
      {
        name: "House Committee on Energy and Commerce",
        chamber: "House",
        role: "Primary",
        referralDate: "2023-09-20"
      },
      {
        name: "House Committee on Natural Resources",
        chamber: "House",
        role: "Secondary",
        referralDate: "2023-09-20"
      }
    ],
    actions: [
      {
        date: "2023-09-20",
        description: "Referred to House Committee on Energy and Commerce",
        chamber: "House"
      },
      {
        date: "2023-09-15",
        description: "Introduced in House",
        chamber: "House"
      }
    ],
    amendments: [
      {
        number: "H.Amdt. 123",
        sponsor: "John Doe",
        purpose: "To include provisions for rural energy development",
        status: "Agreed to",
        date: "2023-10-01"
      }
    ],
    relatedBills: [
      {
        number: "S. 567",
        title: "Clean Energy Infrastructure Act",
        relationship: "Companion",
        congress: "117th"
      },
      {
        number: "H.R. 789",
        title: "Green Jobs Creation Act",
        relationship: "Related",
        congress: "117th"
      }
    ],
    texts: [
      {
        type: "Introduced",
        date: "2023-09-15",
        format: "PDF",
        url: "#",
        pages: 45
      },
      {
        type: "Committee Print",
        date: "2023-10-01",
        format: "PDF",
        url: "#",
        pages: 52
      }
    ]
  };

  return (
      <div className="min-h-screen bg-muted/30">
        <div className="bg-background border-b">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
            <div className="space-y-4">
              <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
                <div>
                  <h1 className="text-2xl sm:text-3xl font-bold">{bill.billNumber} - {bill.title}</h1>
                  <p className="text-base sm:text-lg text-muted-foreground">
                    {bill.type} • {bill.congress}
                  </p>
                </div>
                <Badge variant="default">{bill.status}</Badge>
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
                      <p className="text-muted-foreground">{bill.summary}</p>
                    </CardContent>
                  </Card>

                  {/* Subjects */}
                  <Card>
                    <CardContent className="pt-6">
                      <h2 className="text-xl font-semibold mb-4">Subjects</h2>
                      <div className="flex flex-wrap gap-2">
                        {bill.subjects.map((subject, index) => (
                            <Badge key={index} variant="secondary">
                              <Tag className="h-3 w-3 mr-1" />
                              {subject}
                            </Badge>
                        ))}
                      </div>
                    </CardContent>
                  </Card>

                  {/* Latest Action */}
                  <Card>
                    <CardContent className="pt-6">
                      <h2 className="text-xl font-semibold mb-4">Latest Action</h2>
                      <div className="space-y-2">
                        <p className="text-muted-foreground">{bill.latestAction.description}</p>
                        <p className="text-sm text-muted-foreground">
                          {new Date(bill.latestAction.date).toLocaleDateString()}
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
                            {new Date(bill.introducedDate).toLocaleDateString()}
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
                      <div className="flex items-center gap-4">
                        <div className="relative h-16 w-16 rounded-lg overflow-hidden">
                          <Image
                              src={bill.sponsor.imageUrl}
                              alt={bill.sponsor.name}
                              fill
                              className="object-cover"
                          />
                        </div>
                        <div>
                          <Link
                              href={`/politicians/${bill.sponsor.name.toLowerCase().replace(/\s+/g, "-")}`}
                              className="font-medium hover:text-primary"
                          >
                            {bill.sponsor.name}
                          </Link>
                          <div className="flex items-center gap-2 mt-1">
                            <Badge variant={getPartyBadgeVariant(bill.sponsor.party)}>
                              {bill.sponsor.party}
                            </Badge>
                            <span className="text-sm text-muted-foreground">
                            {bill.sponsor.state}-{bill.sponsor.district}
                          </span>
                          </div>
                        </div>
                      </div>
                    </CardContent>
                  </Card>

                  {/* Co-Sponsors */}
                  <Card>
                    <CardContent className="pt-6">
                      <h2 className="text-xl font-semibold mb-4">
                        Co-Sponsors ({bill.cosponsors.length})
                      </h2>
                      <div className="space-y-4">
                        {bill.cosponsors.map((cosponsor, index) => (
                            <div key={index} className="flex items-center justify-between">
                              <Link
                                  href={`/politicians/${cosponsor.name.toLowerCase().replace(/\s+/g, "-")}`}
                                  className="hover:text-primary"
                              >
                                {cosponsor.name}
                              </Link>
                              <div className="flex items-center gap-2">
                                <Badge variant={getPartyBadgeVariant(cosponsor.party)}>
                                  {cosponsor.party}
                                </Badge>
                                <span className="text-sm text-muted-foreground">
                              {cosponsor.state}-{cosponsor.district}
                            </span>
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
                    {bill.texts.map((text, index) => (
                        <div key={index} className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b pb-4 last:border-0 last:pb-0">
                          <div>
                            <p className="font-medium">{text.type}</p>
                            <p className="text-sm text-muted-foreground">
                              {new Date(text.date).toLocaleDateString()} • {text.pages} pages
                            </p>
                          </div>
                          <Button variant="outline" asChild>
                            <Link href={text.url}>
                              <Download className="h-4 w-4 mr-2" />
                              Download {text.format}
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
                            <p className="text-muted-foreground">{action.description}</p>
                            <Badge variant="outline">{action.chamber}</Badge>
                          </div>
                          <p className="text-sm text-muted-foreground shrink-0">
                            {new Date(action.date).toLocaleDateString()}
                          </p>
                        </div>
                    ))}
                  </div>
                </CardContent>
              </Card>
            </TabsContent>

            <TabsContent value="amendments">
              <Card>
                <CardContent className="pt-6">
                  <h2 className="text-xl font-semibold mb-4">Amendments</h2>
                  <div className="space-y-6">
                    {bill.amendments.map((amendment, index) => (
                        <div key={index} className="border-b pb-6 last:border-0 last:pb-0">
                          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-4">
                            <div>
                              <p className="font-medium">{amendment.number}</p>
                              <p className="text-sm text-muted-foreground">
                                Sponsored by {amendment.sponsor}
                              </p>
                            </div>
                            <Badge variant={amendment.status === "Agreed to" ? "default" : "secondary"}>
                              {amendment.status}
                            </Badge>
                          </div>
                          <p className="text-muted-foreground">{amendment.purpose}</p>
                          <p className="text-sm text-muted-foreground mt-2">
                            {new Date(amendment.date).toLocaleDateString()}
                          </p>
                        </div>
                    ))}
                  </div>
                </CardContent>
              </Card>
            </TabsContent>

            <TabsContent value="committees">
              <Card>
                <CardContent className="pt-6">
                  <h2 className="text-xl font-semibold mb-4">Committee Assignments</h2>
                  <div className="space-y-6">
                    {bill.committees.map((committee, index) => (
                        <div key={index} className="flex flex-col sm:flex-row justify-between gap-4 border-b pb-6 last:border-0 last:pb-0">
                          <div className="space-y-2">
                            <Link
                                href={`/committees/${committee.name.toLowerCase().replace(/\s+/g, "-")}`}
                                className="font-medium hover:text-primary"
                            >
                              {committee.name}
                            </Link>
                            <div className="flex flex-wrap gap-2">
                              <Badge variant="outline">{committee.chamber}</Badge>
                              <Badge variant="secondary">{committee.role}</Badge>
                            </div>
                          </div>
                          <p className="text-sm text-muted-foreground shrink-0">
                            Referred on {new Date(committee.referralDate).toLocaleDateString()}
                          </p>
                        </div>
                    ))}
                  </div>
                </CardContent>
              </Card>
            </TabsContent>

            <TabsContent value="related">
              <Card>
                <CardContent className="pt-6">
                  <h2 className="text-xl font-semibold mb-4">Related Bills</h2>
                  <div className="space-y-6">
                    {bill.relatedBills.map((relatedBill, index) => (
                        <div key={index} className="flex flex-col sm:flex-row justify-between gap-4 border-b pb-6 last:border-0 last:pb-0">
                          <div className="space-y-2">
                            <Link
                                href={`/bills/${relatedBill.number.toLowerCase().replace(/\s+/g, "-")}`}
                                className="font-medium hover:text-primary"
                            >
                              {relatedBill.number} - {relatedBill.title}
                            </Link>
                            <div className="flex flex-wrap gap-2">
                              <Badge variant="outline">{relatedBill.congress}</Badge>
                              <Badge variant="secondary">{relatedBill.relationship}</Badge>
                            </div>
                          </div>
                        </div>
                    ))}
                  </div>
                </CardContent>
              </Card>
            </TabsContent>
          </Tabs>
        </div>
      </div>
  );
}