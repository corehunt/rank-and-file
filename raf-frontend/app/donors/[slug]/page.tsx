import { Card, CardContent } from "@/components/ui/card";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Badge } from "@/components/ui/badge";

export function generateStaticParams() {
  return [
    { slug: "tech-innovation-pac" },
    { slug: "global-energy-corp" }
  ];
}

export default function DonorPage({ params }: { params: { slug: string } }) {
  const donor = {
    name: "Tech Innovation PAC",
    type: "Political Action Committee",
    totalDonations: "$2.5M",
    description: "A leading technology industry political action committee focused on promoting innovation and digital policy.",
    industries: ["Technology", "Communications"],
    topRecipients: [
      { name: "Jane Smith", amount: "$150,000" },
      { name: "John Doe", amount: "$125,000" }
    ]
  };

  return (
    <div className="min-h-screen bg-muted/30">
      <div className="bg-background border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <h1 className="text-3xl font-bold">{donor.name}</h1>
              <Badge variant="outline">{donor.type}</Badge>
            </div>
            <p className="text-lg text-muted-foreground">
              Total Donations: <span className="text-primary font-bold">{donor.totalDonations}</span>
            </p>
            <div className="flex flex-wrap gap-2">
              {donor.industries.map((industry, index) => (
                <Badge key={index} variant="secondary">{industry}</Badge>
              ))}
            </div>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <Tabs defaultValue="overview" className="space-y-6">
          <TabsList>
            <TabsTrigger value="overview">Overview</TabsTrigger>
            <TabsTrigger value="recipients">Recipients</TabsTrigger>
            <TabsTrigger value="timeline">Timeline</TabsTrigger>
            <TabsTrigger value="analysis">Analysis</TabsTrigger>
          </TabsList>

          <TabsContent value="overview">
            <Card>
              <CardContent className="pt-6">
                <p className="text-muted-foreground">{donor.description}</p>
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="recipients">
            <Card>
              <CardContent className="pt-6">
                <div className="space-y-4">
                  {donor.topRecipients.map((recipient, index) => (
                    <div key={index} className="flex justify-between items-center">
                      <span>{recipient.name}</span>
                      <span className="font-bold">{recipient.amount}</span>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="timeline">
            <Card>
              <CardContent className="pt-6">
                <p className="text-muted-foreground">Donation timeline will be displayed here.</p>
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="analysis">
            <Card>
              <CardContent className="pt-6">
                <p className="text-muted-foreground">Detailed analysis of donation patterns will be displayed here.</p>
              </CardContent>
            </Card>
          </TabsContent>
        </Tabs>
      </div>
    </div>
  );
}