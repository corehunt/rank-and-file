import { Card, CardContent } from "@/components/ui/card";

export default function FAQPage() {
  const faqs = [
    {
      category: "General",
      questions: [
        {
          q: "What is Rank and File?",
          a: "Rank and File is a comprehensive platform that aggregates and simplifies data on U.S. politicians, providing transparent insights into their legislative activities, sponsorships, corporate donors, and financial disclosures."
        },
        {
          q: "Is Rank and File politically affiliated?",
          a: "No, we are strictly non-partisan and committed to providing objective, unbiased data to help citizens make informed decisions."
        },
        {
          q: "How can I get started?",
          a: "You can start exploring our platform immediately without an account. However, creating a free account allows you to save searches, set up alerts, and access additional features."
        }
      ]
    },
    {
      category: "Data & Updates",
      questions: [
        {
          q: "How often is the data updated?",
          a: "We update our data in real-time for congressional votes, daily for campaign finance data, and weekly for stock trading information. All data undergoes thorough verification processes."
        },
        {
          q: "Where does your data come from?",
          a: "Our data comes from official government sources including Congressional Records, FEC reports, SEC filings, and Office of Government Ethics disclosures."
        },
        {
          q: "How far back does your historical data go?",
          a: "Our database includes comprehensive records dating back to 2000, with selected historical data available for significant legislative events before that date."
        }
      ]
    },
    {
      category: "Account & Features",
      questions: [
        {
          q: "Do I need to create an account?",
          a: "While basic browsing is available to all users, creating an account gives you access to features like saved searches, custom alerts, and personalized dashboards."
        },
        {
          q: "Is there a mobile app?",
          a: "Currently, we offer a mobile-responsive website. A dedicated mobile app is in development and will be released soon."
        },
        {
          q: "Can I export data from the platform?",
          a: "Yes, registered users can export data in various formats (CSV, JSON, PDF) for personal use. For bulk data access, please see our API documentation."
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
          a: "You can report technical issues through our contact form or by emailing support@rankandfile.com. We aim to respond within 24 hours."
        },
        {
          q: "Is there an API available?",
          a: "Yes, we offer a comprehensive API for developers. Visit our API documentation page for more information about access and integration."
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