export default function PrivacyPage() {
  return (
      <div className="min-h-screen bg-muted/30">
        <div className="bg-background border-b">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
            <div className="text-center max-w-3xl mx-auto">
              <h1 className="text-4xl font-bold mb-4">Privacy Policy</h1>
              <p className="text-xl text-muted-foreground">
                We are committed to protecting your privacy.
              </p>
            </div>
          </div>
        </div>

        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
          <div className="prose prose-neutral dark:prose-invert max-w-none">
            <section className="mb-12">
              <h2 className="text-2xl font-bold mb-4">Information We Collect</h2>
              <p className="text-muted-foreground mb-4">
                Our platform is designed to provide political data without requiring you to create an account or share any personal information. We do not collect, store, or process any data that can personally identify you.
              </p>
              <p className="text-muted-foreground">
                We may use non-personally identifiable analytics solely to improve the performance and functionality of our website.
              </p>
            </section>

            <section className="mb-12">
              <h2 className="text-2xl font-bold mb-4">How We Use Information</h2>
              <p className="text-muted-foreground mb-4">
                Since we do not collect any personal information, we do not use or share any such data. Any analytics we perform are entirely aggregated and do not contain details that could identify you.
              </p>
            </section>

            <section className="mb-12">
              <h2 className="text-2xl font-bold mb-4">Data Security</h2>
              <p className="text-muted-foreground mb-4">
                Although we do not store personal information, we take appropriate measures to protect our website and its functionality. These measures include:
              </p>
              <ul className="list-disc pl-6 space-y-2 text-muted-foreground">
                <li>Encryption of data transmitted on our website</li>
                <li>Regular updates and security assessments</li>
                <li>Monitoring for unauthorized access or activity</li>
              </ul>
            </section>

            <section className="mb-12">
              <h2 className="text-2xl font-bold mb-4">Your Rights</h2>
              <p className="text-muted-foreground mb-4">
                Because we do not collect or retain personal information, there is no personal data to access, correct, or delete. However, if you have any concerns regarding our privacy practices, please do not hesitate to contact us.
              </p>
            </section>

            <section className="mb-12">
              <h2 className="text-2xl font-bold mb-4">Disclaimer and Limitation of Liability</h2>
              <p className="text-muted-foreground mb-4">
                Our services are provided "as is" without any warranties, either express or implied. To the fullest extent permitted by law, we shall not be liable for any indirect, incidental, or consequential damages arising from your use of our website or from any inability to access its content. We reserve the right to modify this Privacy Policy at any time without prior notice.
              </p>
            </section>

            <section className="mb-12">
              <h2 className="text-2xl font-bold mb-4">Contact Us</h2>
              <p className="text-muted-foreground">
                If you have any questions about our Privacy Policy, please contact us at{" "}
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