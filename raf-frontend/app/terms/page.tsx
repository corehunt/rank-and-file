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
              By accessing and using Rank and File, you accept and agree to be bound by the terms and provisions of this agreement. If you do not agree to these terms, please do not use our platform.
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

          <section className="mb-12">
            <h2 className="text-2xl font-bold mb-4">3. User Accounts</h2>
            <p className="text-muted-foreground mb-4">
              When you create an account with us, you must provide accurate and complete information. You are responsible for:
            </p>
            <ul className="list-disc pl-6 space-y-2 text-muted-foreground">
              <li>Maintaining the confidentiality of your account</li>
              <li>Restricting access to your account</li>
              <li>All activities that occur under your account</li>
            </ul>
          </section>

          <section className="mb-12">
            <h2 className="text-2xl font-bold mb-4">4. Data Usage</h2>
            <p className="text-muted-foreground">
              Our platform aggregates publicly available political data. While we strive for accuracy, we cannot guarantee the completeness or timeliness of information. Users should verify critical information from official sources.
            </p>
          </section>

          <section className="mb-12">
            <h2 className="text-2xl font-bold mb-4">5. Limitations</h2>
            <p className="text-muted-foreground">
              In no event shall Rank and File be liable for any damages arising out of the use or inability to use our platform, even if we have been notified of the possibility of such damages.
            </p>
          </section>

          <section className="mb-12">
            <h2 className="text-2xl font-bold mb-4">6. Modifications</h2>
            <p className="text-muted-foreground">
              We reserve the right to revise these terms of service at any time without notice. By using this platform, you agree to be bound by the current version of these terms of service.
            </p>
          </section>

          <section className="mb-12">
            <h2 className="text-2xl font-bold mb-4">7. Contact</h2>
            <p className="text-muted-foreground">
              If you have any questions about these Terms of Service, please contact us at{" "}
              <a href="mailto:legal@rankandfile.com" className="text-primary hover:underline">
                legal@rankandfile.com
              </a>
            </p>
          </section>

          <div className="text-sm text-muted-foreground text-center mt-16">
            Last updated: {new Date().toLocaleDateString()}
          </div>
        </div>
      </div>
    </div>
  );
}