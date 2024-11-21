import Image from "next/image";
import { Card, CardContent } from "@/components/ui/card";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Badge } from "@/components/ui/badge";
import { Phone, Mail, MapPin } from "lucide-react";

export function generateStaticParams() {
  return [
    { slug: "jane-smith" },
    { slug: "john-doe" }
  ];
}

export default function PoliticianProfile({ params }: { params: { slug: string } }) {
  const politician = {
    name: "Jane Smith",
    state: "CA",
    party: "Democratic",
    district: "12th District",
    chamber: "House of Representatives",
    imageUrl: "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?auto=format&fit=crop&q=80&w=200&h=200",
    biography: "Serving since 2019, Representative Smith has focused on environmental protection and healthcare reform. She currently serves on the House Committee on Energy and Commerce and the House Committee on Natural Resources. Prior to her congressional service, she worked as an environmental lawyer and served two terms in the California State Assembly.",
    partyStartYear: 2018,
    membershipStatus: "Active",
    yearsActive: "2019 - Present",
    officeLocation: {
      dc: "123 Cannon House Office Building, Washington, DC 20515",
      district: "456 Main Street, Suite 789, San Francisco, CA 94105"
    },
    phone: {
      dc: "(202) 225-1234",
      district: "(415) 555-0123"
    },
    congressionalRecord: {
      sponsoredBills: [
        { id: "HR1234", title: "Clean Energy Act of 2023", status: "In Committee" },
        { id: "HR5678", title: "Healthcare Access Improvement Act", status: "Passed House" }
      ],
      recentVotes: [
        { bill: "HR2468", title: "Infrastructure Investment Act", vote: "Yea", date: "2024-01-15" },
        { bill: "HR3579", title: "Budget Resolution 2024", vote: "Nay", date: "2024-01-10" }
      ],
      committees: [
        { name: "House Committee on Energy and Commerce", role: "Member" },
        { name: "House Committee on Natural Resources", role: "Vice Chair" }
      ]
    }
  };

  return (
    <div className="min-h-screen bg-muted/30">
      <div className="bg-background border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <div className="flex flex-col md:flex-row gap-6">
            <div className="relative h-48 w-48 rounded-lg overflow-hidden flex-shrink-0 mx-auto md:mx-0">
              <Image
                src={politician.imageUrl}
                alt={politician.name}
                fill
                className="object-cover"
              />
            </div>
            <div className="flex-grow">
              <div className="grid md:grid-cols-3 gap-6">
                {/* First Column: Basic Info */}
                <div className="space-y-2 text-center md:text-left">
                  <h1 className="text-3xl font-bold">{politician.name}</h1>
                  <p className="text-lg text-muted-foreground">{politician.district}, {politician.state}</p>
                  <div className="flex flex-wrap gap-2 justify-center md:justify-start">
                    <Badge variant={politician.party === "Democratic" ? "default" : "destructive"}>
                      {politician.party}
                    </Badge>
                    <Badge variant="outline">{politician.membershipStatus}</Badge>
                  </div>
                </div>

                {/* Second Column: Chamber & Years */}
                <div className="grid grid-cols-2 md:block text-center md:text-left gap-4">
                  <div>
                    <p className="text-sm font-medium">Chamber</p>
                    <p className="text-muted-foreground">{politician.chamber}</p>
                  </div>
                  <div>
                    <p className="text-sm font-medium">Years Active</p>
                    <p className="text-muted-foreground">{politician.yearsActive}</p>
                  </div>
                </div>

                {/* Third Column: Contact Info */}
                <div className="space-y-3 text-left">
                  <div className="flex items-start gap-2">
                    <MapPin className="h-5 w-5 mt-0.5 text-muted-foreground" />
                    <div className="text-sm">
                      <p className="font-medium">Offices</p>
                      <p className="text-muted-foreground">DC: {politician.officeLocation.dc}</p>
                      <p className="text-muted-foreground">District: {politician.officeLocation.district}</p>
                    </div>
                  </div>
                  <div className="flex items-start gap-2">
                    <Phone className="h-5 w-5 mt-0.5 text-muted-foreground" />
                    <div className="text-sm">
                      <p className="font-medium">Phone</p>
                      <p className="text-muted-foreground">DC: {politician.phone.dc}</p>
                      <p className="text-muted-foreground">District: {politician.phone.district}</p>
                    </div>
                  </div>
                </div>
              </div>

              {/* Biography Row */}
              <div className="mt-6">
                <p className="text-muted-foreground md:text-left">{politician.biography}</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <Tabs defaultValue="record" className="space-y-6">
          <TabsList>
            <TabsTrigger value="record">Congressional Record</TabsTrigger>
            <TabsTrigger value="votes">Voting History</TabsTrigger>
            <TabsTrigger value="bills">Sponsored Bills</TabsTrigger>
            <TabsTrigger value="finances">Financial Activity</TabsTrigger>
          </TabsList>

          <TabsContent value="record">
            <div className="space-y-6">
              <Card>
                <CardContent className="pt-6">
                  <h2 className="text-xl font-semibold mb-4">Committee Memberships</h2>
                  <div className="space-y-4">
                    {politician.congressionalRecord.committees.map((committee, index) => (
                      <div key={index} className="flex justify-between items-center border-b pb-4 last:border-0 last:pb-0">
                        <div>
                          <p className="font-medium">{committee.name}</p>
                          <p className="text-sm text-muted-foreground">{committee.role}</p>
                        </div>
                      </div>
                    ))}
                  </div>
                </CardContent>
              </Card>

              <Card>
                <CardContent className="pt-6">
                  <h2 className="text-xl font-semibold mb-4">Recent Legislative Activity</h2>
                  <div className="space-y-6">
                    <div>
                      <h3 className="font-medium mb-3">Sponsored Bills</h3>
                      <div className="space-y-4">
                        {politician.congressionalRecord.sponsoredBills.map((bill, index) => (
                          <div key={index} className="flex justify-between items-center border-b pb-4 last:border-0 last:pb-0">
                            <div>
                              <p className="font-medium">{bill.title}</p>
                              <p className="text-sm text-muted-foreground">{bill.id}</p>
                            </div>
                            <Badge variant="secondary">{bill.status}</Badge>
                          </div>
                        ))}
                      </div>
                    </div>
                    <div>
                      <h3 className="font-medium mb-3">Recent Votes</h3>
                      <div className="space-y-4">
                        {politician.congressionalRecord.recentVotes.map((vote, index) => (
                          <div key={index} className="flex justify-between items-center border-b pb-4 last:border-0 last:pb-0">
                            <div>
                              <p className="font-medium">{vote.title}</p>
                              <p className="text-sm text-muted-foreground">{vote.bill}</p>
                            </div>
                            <div className="text-right">
                              <Badge variant={vote.vote === "Yea" ? "default" : "secondary"}>
                                {vote.vote}
                              </Badge>
                              <p className="text-sm text-muted-foreground mt-1">
                                {new Date(vote.date).toLocaleDateString()}
                              </p>
                            </div>
                          </div>
                        ))}
                      </div>
                    </div>
                  </div>
                </CardContent>
              </Card>
            </div>
          </TabsContent>

          <TabsContent value="votes">
            <Card>
              <CardContent className="pt-6">
                <p className="text-muted-foreground">Detailed voting record will be displayed here.</p>
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="bills">
            <Card>
              <CardContent className="pt-6">
                <p className="text-muted-foreground">Complete list of sponsored and co-sponsored bills will be displayed here.</p>
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="finances">
            <Card>
              <CardContent className="pt-6">
                <p className="text-muted-foreground">Campaign finances and stock trades will be displayed here.</p>
              </CardContent>
            </Card>
          </TabsContent>
        </Tabs>
      </div>
    </div>
  );
}