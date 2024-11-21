export default function ApiPage() {
  return (
    <div className="min-h-screen bg-muted/30">
      <div className="bg-background border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
          <div className="text-center max-w-3xl mx-auto">
            <h1 className="text-4xl font-bold mb-4">API Access</h1>
            <p className="text-xl text-muted-foreground">
              Access our comprehensive political data through our REST API.
            </p>
          </div>
        </div>
      </div>

      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
        <div className="prose prose-neutral dark:prose-invert max-w-none">
          <section className="mb-12">
            <h2 className="text-2xl font-bold mb-4">Getting Started</h2>
            <p className="text-muted-foreground mb-4">
              To start using our API:
            </p>
            <ol className="list-decimal pl-6 space-y-2 text-muted-foreground">
              <li>Sign up for an API key through your account dashboard</li>
              <li>Review our API documentation</li>
              <li>Test your integration using our sandbox environment</li>
              <li>Monitor your usage through our developer portal</li>
            </ol>
          </section>

          <section className="mb-12">
            <h2 className="text-2xl font-bold mb-4">Available Endpoints</h2>
            <div className="space-y-4">
              <div>
                <h3 className="text-xl font-semibold">Politicians</h3>
                <p className="text-muted-foreground">Access detailed information about politicians, including:</p>
                <ul className="list-disc pl-6 text-muted-foreground">
                  <li>Biographical data</li>
                  <li>Voting records</li>
                  <li>Committee memberships</li>
                  <li>Financial disclosures</li>
                </ul>
              </div>
              <div>
                <h3 className="text-xl font-semibold">Bills</h3>
                <p className="text-muted-foreground">Retrieve information about legislation:</p>
                <ul className="list-disc pl-6 text-muted-foreground">
                  <li>Bill text and summaries</li>
                  <li>Voting results</li>
                  <li>Sponsor information</li>
                  <li>Amendment history</li>
                </ul>
              </div>
              <div>
                <h3 className="text-xl font-semibold">Campaign Finance</h3>
                <p className="text-muted-foreground">Access donation and spending data:</p>
                <ul className="list-disc pl-6 text-muted-foreground">
                  <li>Individual contributions</li>
                  <li>PAC donations</li>
                  <li>Campaign expenditures</li>
                  <li>Donor analytics</li>
                </ul>
              </div>
            </div>
          </section>

          <section className="mb-12">
            <h2 className="text-2xl font-bold mb-4">Rate Limits</h2>
            <div className="space-y-4">
              <p className="text-muted-foreground">Our API plans include:</p>
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div className="p-4 border rounded-lg">
                  <h3 className="font-semibold mb-2">Basic</h3>
                  <ul className="text-sm text-muted-foreground">
                    <li>1,000 requests/day</li>
                    <li>5 requests/second</li>
                  </ul>
                </div>
                <div className="p-4 border rounded-lg">
                  <h3 className="font-semibold mb-2">Pro</h3>
                  <ul className="text-sm text-muted-foreground">
                    <li>10,000 requests/day</li>
                    <li>20 requests/second</li>
                  </ul>
                </div>
                <div className="p-4 border rounded-lg">
                  <h3 className="font-semibold mb-2">Enterprise</h3>
                  <ul className="text-sm text-muted-foreground">
                    <li>Custom limits</li>
                    <li>Dedicated support</li>
                  </ul>
                </div>
              </div>
            </div>
          </section>

          <section className="mb-12">
            <h2 className="text-2xl font-bold mb-4">Support</h2>
            <p className="text-muted-foreground">
              Need help with our API? Contact our developer support team at{" "}
              <a href="mailto:api@rankandfile.com" className="text-primary hover:underline">
                api@rankandfile.com
              </a>
            </p>
          </section>
        </div>
      </div>
    </div>
  );
}