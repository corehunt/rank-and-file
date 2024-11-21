import { Card, CardContent } from "@/components/ui/card";

export default function MethodologyPage() {
  const sections = [
    {
      title: "Data Collection",
      content: "Our data is collected from various official sources, including:",
      items: [
        "Congressional Records and Databases",
        "Federal Election Commission (FEC) Reports",
        "Securities and Exchange Commission (SEC) Filings",
        "Office of Government Ethics (OGE) Disclosures",
        "State Electoral Databases"
      ]
    },
    {
      title: "Verification Process",
      content: "Our multi-step verification process ensures data accuracy:",
      items: [
        "Automated data validation and cross-referencing",
        "Manual review by our team of political researchers",
        "Regular audits and updates",
        "Correction and feedback mechanisms"
      ]
    },
    {
      title: "Analysis Methods",
      content: "We employ various analytical techniques to provide insights:",
      items: [
        "Pattern recognition in voting behavior",
        "Network analysis of donor relationships",
        "Statistical analysis of financial transactions",
        "Temporal analysis of legislative activities"
      ]
    },
    {
      title: "Data Updates",
      content: "Our commitment to timely information:",
      items: [
        "Real-time updates for congressional votes",
        "Daily updates for campaign finance data",
        "Weekly updates for stock trading information",
        "Monthly comprehensive data audits"
      ]
    },
    {
      title: "Quality Assurance",
      content: "We maintain high standards through:",
      items: [
        "Peer review of methodologies",
        "Regular consultation with political science experts",
        "Transparent error correction procedures",
        "Open feedback channels with users"
      ]
    }
  ];

  return (
    <div className="min-h-screen bg-muted/30">
      <div className="bg-background border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
          <div className="text-center max-w-3xl mx-auto">
            <h1 className="text-4xl font-bold mb-4">Our Methodology</h1>
            <p className="text-xl text-muted-foreground">
              Learn about our data collection, verification, and analysis processes.
            </p>
          </div>
        </div>
      </div>

      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
        <div className="grid gap-8">
          {sections.map((section, index) => (
            <Card key={index}>
              <CardContent className="pt-6">
                <h2 className="text-2xl font-bold mb-4">{section.title}</h2>
                <p className="text-muted-foreground mb-4">{section.content}</p>
                <ul className="grid gap-2">
                  {section.items.map((item, itemIndex) => (
                    <li key={itemIndex} className="flex items-start">
                      <span className="mr-2 mt-1.5 h-1.5 w-1.5 rounded-full bg-primary flex-shrink-0" />
                      <span className="text-muted-foreground">{item}</span>
                    </li>
                  ))}
                </ul>
              </CardContent>
            </Card>
          ))}
        </div>
      </div>
    </div>
  );
}