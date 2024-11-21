import { Card, CardContent } from "@/components/ui/card";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Badge } from "@/components/ui/badge";

export function generateStaticParams() {
  return [
    { slug: "house-committee-on-energy-and-commerce" },
    { slug: "senate-committee-on-foreign-relations" }
  ];
}

export default function CommitteePage({ params }: { params: { slug: string } }) {
  // In production, fetch committee data based on slug
  const committee = {
    name: "House Committee on Energy and Commerce",
    chamber: "House",
    chair: "John Smith",
    rankingMember: "Jane Doe",
    memberCount: 52,
    description: "The Committee on Energy and Commerce is the oldest standing legislative committee in the U.S. House of Representatives and is vested with the broadest jurisdiction of any congressional authorizing committee.",
    jurisdiction: [
      "Public health and quarantine",
      "Health facilities and health care",
      "Environmental protection",
      "Energy policy",
      "Interstate and foreign commerce"
    ],
    members: [
      { name: "John Smith", role: "Chair", party: "Democratic" },
      { name: "Jane Doe", role: "Ranking Member", party: "Republican" }
    ]
  };

  return (
    <div className="min-h-screen bg-muted/30">
      <div className="bg-background border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <h1 className="text-3xl font-bold">{committee.name}</h1>
              <Badge variant={committee.chamber === "House" ? "default" : "secondary"}>
                {committee.chamber}
              </Badge>
            </div>
            <p className="text-lg text-muted-foreground">
              Chair: {committee.chair} • Ranking Member: {committee.rankingMember}
            </p>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <Tabs defaultValue="overview" className="space-y-6">
          <TabsList>
            <TabsTrigger value="overview">Overview</TabsTrigger>
            <TabsTrigger value="members">Members</TabsTrigger>
            <TabsTrigger value="hearings">Hearings</TabsTrigger>
            <TabsTrigger value="legislation">Legislation</TabsTrigger>
          </TabsList>

          <TabsContent value="overview">
            <Card>
              <CardContent className="pt-6 space-y-6">
                <div>
                  <h2 className="text-xl font-semibold mb-2">About</h2>
                  <p className="text-muted-foreground">{committee.description}</p>
                </div>
                <div>
                  <h2 className="text-xl font-semibold mb-2">Jurisdiction</h2>
                  <ul className="list-disc list-inside space-y-1">
                    {committee.jurisdiction.map((item, index) => (
                      <li key={index} className="text-muted-foreground">{item}</li>
                    ))}
                  </ul>
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="members">
            <Card>
              <CardContent className="pt-6">
                <div className="space-y-4">
                  {committee.members.map((member, index) => (
                    <div key={index} className="flex justify-between items-center">
                      <div>
                        <span className="font-medium">{member.name}</span>
                        <span className="text-sm text-muted-foreground ml-2">
                          {member.role}
                        </span>
                      </div>
                      <Badge variant={member.party === "Democratic" ? "default" : "destructive"}>
                        {member.party}
                      </Badge>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="hearings">
            <Card>
              <CardContent className="pt-6">
                <p className="text-muted-foreground">Committee hearings will be displayed here.</p>
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="legislation">
            <Card>
              <CardContent className="pt-6">
                <p className="text-muted-foreground">Committee legislation will be displayed here.</p>
              </CardContent>
            </Card>
          </TabsContent>
        </Tabs>
      </div>
    </div>
  );
}