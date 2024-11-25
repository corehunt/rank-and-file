import Image from "next/image";
import Link from "next/link";
import { Card, CardContent } from "@/components/ui/card";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Badge } from "@/components/ui/badge";

export function generateStaticParams() {
  // In production, this would fetch from your API/database
  return [
    { slug: "h-r-1234-clean-energy-act" },
    { slug: "s-789-infrastructure-investment-act" }
  ];
}

const getPartyBadgeVariant = (party: string) => {
  switch (party) {
    case "Democratic":
      return "default"; // Blue
    case "Republican":
      return "destructive"; // Red
    default:
      return "outline";
  }
};

export default function BillPage({ params }: { params: { slug: string } }) {
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
    summary: "A bill to promote renewable energy development and reduce carbon emissions through federal incentives and regulations.",
    policyArea: "Energy and Environment",
    sponsor: {
      name: "Jane Smith",
      party: "Democratic",
      state: "CA"
    },
    cosponsors: [
      { name: "John Doe", party: "Republican", state: "TX" },
      { name: "Sarah Johnson", party: "Democratic", state: "NY" }
    ],
    committees: [
      {
        name: "House Committee on Energy and Commerce",
        chamber: "House",
        url: "/committees/house-energy-commerce"
      }
    ],
    actions: [
      {
        date: "2023-09-20",
        description: "Referred to House Committee on Energy and Commerce"
      },
      {
        date: "2023-09-15",
        description: "Introduced in House"
      }
    ],
    texts: [
      {
        type: "Introduced",
        date: "2023-09-15",
        url: "#"
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
          </TabsList>

          <TabsContent value="overview">
            <div className="space-y-6">
              <Card>
                <CardContent className="pt-6">
                  <h2 className="text-xl font-semibold mb-4">Bill Information</h2>
                  <div className="grid gap-6 sm:grid-cols-2">
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
                      <p className="text-sm font-medium">Latest Action</p>
                      <p className="text-muted-foreground">
                        {new Date(bill.latestAction.date).toLocaleDateString()} - {bill.latestAction.description}
                      </p>
                    </div>
                    <div>
                      <p className="text-sm font-medium">Policy Area</p>
                      <p className="text-muted-foreground">{bill.policyArea}</p>
                    </div>
                  </div>
                </CardContent>
              </Card>

              <Card>
                <CardContent className="pt-6">
                  <h2 className="text-xl font-semibold mb-4">Summary</h2>
                  <p className="text-muted-foreground">{bill.summary}</p>
                </CardContent>
              </Card>

              <Card>
                <CardContent className="pt-6">
                  <h2 className="text-xl font-semibold mb-4">Sponsors</h2>
                  <div className="space-y-6">
                    <div>
                      <h3 className="text-sm font-medium mb-3">Primary Sponsor</h3>
                      <div className="flex items-center justify-between border-b pb-4">
                        <Link 
                          href={`/politicians/${bill.sponsor.name.toLowerCase().replace(/\s+/g, "-")}`}
                          className="hover:text-primary"
                        >
                          {bill.sponsor.name}
                        </Link>
                        <div className="text-right">
                          <Badge variant={getPartyBadgeVariant(bill.sponsor.party)}>
                            {bill.sponsor.party}
                          </Badge>
                          <span className="text-sm text-muted-foreground ml-2">{bill.sponsor.state}</span>
                        </div>
                      </div>
                    </div>
                    <div>
                      <h3 className="text-sm font-medium mb-3">Co-Sponsors ({bill.cosponsors.length})</h3>
                      <div className="space-y-4">
                        {bill.cosponsors.map((cosponsor, index) => (
                          <div key={index} className="flex items-center justify-between border-b pb-4 last:border-0 last:pb-0">
                            <Link 
                              href={`/politicians/${cosponsor.name.toLowerCase().replace(/\s+/g, "-")}`}
                              className="hover:text-primary"
                            >
                              {cosponsor.name}
                            </Link>
                            <div className="text-right">
                              <Badge variant={getPartyBadgeVariant(cosponsor.party)}>
                                {cosponsor.party}
                              </Badge>
                              <span className="text-sm text-muted-foreground ml-2">{cosponsor.state}</span>
                            </div>
                          </div>
                        ))}
                      </div>
                    </div>
                  </div>
                </CardContent>
              </Card>

              <Card>
                <CardContent className="pt-6">
                  <h2 className="text-xl font-semibold mb-4">Committee Assignments</h2>
                  <div className="space-y-4">
                    {bill.committees.map((committee, index) => (
                      <div key={index} className="flex items-center justify-between border-b pb-4 last:border-0 last:pb-0">
                        <Link href={committee.url} className="hover:text-primary">
                          {committee.name}
                        </Link>
                        <Badge variant="outline">{committee.chamber}</Badge>
                      </div>
                    ))}
                  </div>
                </CardContent>
              </Card>
            </div>
          </TabsContent>

          <TabsContent value="text">
            <Card>
              <CardContent className="pt-6">
                <div className="space-y-4">
                  {bill.texts.map((text, index) => (
                    <div key={index} className="flex items-center justify-between border-b pb-4 last:border-0 last:pb-0">
                      <div>
                        <p className="font-medium">{text.type}</p>
                        <p className="text-sm text-muted-foreground">
                          {new Date(text.date).toLocaleDateString()}
                        </p>
                      </div>
                      <Link href={text.url} className="text-primary hover:underline">
                        Download PDF
                      </Link>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="actions">
            <Card>
              <CardContent className="pt-6">
                <div className="space-y-4">
                  {bill.actions.map((action, index) => (
                    <div key={index} className="flex items-start justify-between border-b pb-4 last:border-0 last:pb-0">
                      <p className="text-muted-foreground flex-1">{action.description}</p>
                      <p className="text-sm text-muted-foreground ml-4">
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
                <p className="text-muted-foreground">No amendments have been proposed for this bill.</p>
              </CardContent>
            </Card>
          </TabsContent>
        </Tabs>
      </div>
    </div>
  );
}