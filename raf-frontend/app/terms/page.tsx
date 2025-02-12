export default function TermsPage() {
  return (
      <div className="min-h-screen bg-muted/30">
        <div className="bg-background border-b">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
            <div className="text-center max-w-3xl mx-auto">
              <h1 className="text-4xl font-bold mb-4">Terms of Service</h1>
              <p className="text-xl text-muted-foreground">
                Please read these terms carefully before using our platform.
              </p>
            </div>
          </div>
        </div>

        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
          <div className="prose prose-neutral dark:prose-invert max-w-none">
            <section className="mb-12">
              <h2 className="text-2xl font-bold mb-4">1. Acceptance of Terms</h2>
              <p className="text-muted-foreground">
                By accessing and using Rank and File, you agree to be bound by these Terms of Service. If you do not agree with these terms, please do not use our platform.
              </p>
            </section>

            <section className="mb-12">
              <h2 className="text-2xl font-bold mb-4">2. Use License</h2>
              <p className="text-muted-foreground mb-4">
                Permission is granted to temporarily access and use Rank and File for personal, non-commercial purposes. This license does not include:
              </p>
              <ul className="list-disc pl-6 space-y-2 text-muted-foreground">
                <li>Modifying or copying our materials</li>
                <li>Using the material for commercial purposes</li>
                <li>Attempting to reverse engineer any software contained on the platform</li>
                <li>Removing any copyright or proprietary notations</li>
              </ul>
            </section>

            {/* Note: The User Accounts section has been removed as our platform does not require account registration or collect personal data. */}

            <section className="mb-12">
              <h2 className="text-2xl font-bold mb-4">3. Data Usage</h2>
              <p className="text-muted-foreground">
                Rank and File aggregates publicly available political data. While we strive for accuracy, we cannot guarantee the completeness, timeliness, or accuracy of the information provided. Users are responsible for verifying any critical information from official sources.
                <br /><br />
                Please note that our platform does not require you to create an account, and we do not collect, store, or process any personal information.
              </p>
            </section>

            <section className="mb-12">
              <h2 className="text-2xl font-bold mb-4">4. Limitations</h2>
              <p className="text-muted-foreground">
                In no event shall Rank and File be liable for any damages (including, without limitation, direct, indirect, incidental, or consequential damages) arising out of your use of, or inability to use, our platform—even if we have been advised of the possibility of such damages. You use our platform at your own risk.
              </p>
            </section>

            <section className="mb-12">
              <h2 className="text-2xl font-bold mb-4">5. Modifications</h2>
              <p className="text-muted-foreground">
                We reserve the right to revise these Terms of Service at any time without prior notice. By continuing to use the platform, you agree to be bound by the current version of these terms.
              </p>
            </section>

            <section className="mb-12">
              <h2 className="text-2xl font-bold mb-4">6. Contact</h2>
              <p className="text-muted-foreground">
                If you have any questions about these Terms of Service, please contact us at{" "}
                <a href="mailto:contact@rankandfile.us" className="text-primary hover:underline">
                  contact@rankandfile.us
                </a>
              </p>
            </section>

            <div className="text-sm text-muted-foreground text-center mt-16">
              Last updated: 2/12/2025
            </div>
          </div>
        </div>
      </div>
  );
}