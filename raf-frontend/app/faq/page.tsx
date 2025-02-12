import { Card, CardContent } from "@/components/ui/card";

export default function FAQPage() {
  const faqs = [
    {
      category: "General",
      questions: [
        {
          q: "What is Rank and File?",
          a: "Rank and File is a comprehensive platform that aggregates and simplifies data on U.S. politicians, providing transparent insights into their legislative activities and sponsorships."
        },
        {
          q: "Is Rank and File politically affiliated?",
          a: "No, we are strictly non-partisan and committed to providing objective, unbiased data to help users make informed decisions."
        },
        {
          q: "How can I get started?",
          a: "You can start exploring our platform immediately without an account."
        }
      ]
    },
    {
      category: "Data & Updates",
      questions: [
        {
          q: "How often is the data updated?",
          a: "We provide hourly updates for bill data, daily for representative info, and weekly for historical & committee information. All data undergoes thorough verification processes."
        },
        {
          q: "Where does your data come from?",
          a: "Our data currently comes exclusively through the official congress.gov website."
        },
        {
          q: "How far back does your historical data go?",
          a: "Our database includes comprehensive records dating back to 1993."
        }
      ]
    },
    {
      category: "Account & Features",
      questions: [
        {
          q: "Do I need to create an account?",
          a: "While basic browsing is available to all users, we plan to give account creation access to features like saved searches, custom alerts, and personalized dashboards in the future."
        },
        {
          q: "Is there a mobile app?",
          a: "Currently, we offer a mobile-responsive website. A dedicated mobile app is not planned at this time but please let us know if its something you're interested in."
        },
        {
          q: "Can I export data from the platform?",
          a: "Only full text versions of bills are available for download."
        }
      ]
    },
    {
      category: "Technical Support",
      questions: [
        {
          q: "What browsers are supported?",
          a: "We support all modern browsers including Chrome, Firefox, Safari, and Edge. For the best experience, please ensure your browser is up to date."
        },
        {
          q: "How can I report an issue?",
          a: "You can report technical issues by emailing contact@rankandfile.us. We aim to respond within 24 hours."
        },
        {
          q: "Is there an API available?",
          a: "Not yet, but we plan to offer access in the future once we have a more feature rich environment."
        }
      ]
    }
  ];

  return (
    <div className="min-h-screen bg-muted/30">
      <div className="bg-background border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
          <div className="text-center max-w-3xl mx-auto">
            <h1 className="text-4xl font-bold mb-4">Frequently Asked Questions</h1>
            <p className="text-xl text-muted-foreground">
              Find answers to common questions about Rank and File.
            </p>
          </div>
        </div>
      </div>

      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
        <div className="space-y-12">
          {faqs.map((section, index) => (
            <div key={index}>
              <h2 className="text-2xl font-bold mb-6">{section.category}</h2>
              <div className="grid gap-6">
                {section.questions.map((item, itemIndex) => (
                  <Card key={itemIndex}>
                    <CardContent className="pt-6">
                      <h3 className="text-lg font-semibold mb-2">{item.q}</h3>
                      <p className="text-muted-foreground">{item.a}</p>
                    </CardContent>
                  </Card>
                ))}
              </div>
            </div>
          ))}
        </div>

        <div className="mt-16 text-center">
          <p className="text-muted-foreground">
            Still have questions?{" "}
            <a href="/contact" className="text-primary hover:underline">
              Contact our support team
            </a>
          </p>
        </div>
      </div>
    </div>
  );
}