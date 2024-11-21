import { Card, CardContent } from "@/components/ui/card";
import { Scale, Users, BarChart3, Shield } from "lucide-react";

export default function AboutPage() {
  return (
    <div className="min-h-screen bg-muted/30">
      <div className="bg-background border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
          <div className="text-center max-w-3xl mx-auto">
            <Scale className="h-16 w-16 mx-auto text-primary mb-6" />
            <h1 className="text-4xl font-bold mb-4">About Rank and File</h1>
            <p className="text-xl text-muted-foreground">
              Empowering citizens with transparent insights into U.S. politics through comprehensive data analysis and accessible information.
            </p>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-8 mb-16">
          <Card>
            <CardContent className="pt-6">
              <h2 className="text-2xl font-bold mb-4">Our Mission</h2>
              <p className="text-muted-foreground">
                Rank and File is dedicated to empowering everyday citizens by providing them with a reliable platform to conduct their own research on U.S. politicians. We believe in delivering transparent, accurate, and unbiased data to inform more knowledgeable voters and foster a more engaged electorate.
              </p>
            </CardContent>
          </Card>
          <Card>
            <CardContent className="pt-6">
              <h2 className="text-2xl font-bold mb-4">Our Vision</h2>
              <p className="text-muted-foreground">
                We envision a democracy where every citizen has easy access to comprehensive, unbiased information about their representatives. By making political data more accessible and understandable, we aim to strengthen democratic participation and accountability.
              </p>
            </CardContent>
          </Card>
        </div>

        <div className="space-y-16">
          <div>
            <h2 className="text-2xl font-bold mb-8 text-center">Core Principles</h2>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
              <div className="text-center space-y-4">
                <Shield className="h-12 w-12 mx-auto text-primary" />
                <h3 className="text-xl font-semibold">Transparency</h3>
                <p className="text-muted-foreground">
                  We prioritize clear, accurate, and accessible data, enabling users to make informed decisions based on objective information.
                </p>
              </div>
              <div className="text-center space-y-4">
                <BarChart3 className="h-12 w-12 mx-auto text-primary" />
                <h3 className="text-xl font-semibold">Comprehensive Insights</h3>
                <p className="text-muted-foreground">
                  Our platform offers a wide variety of data points, making it a robust resource for understanding politicians' actions and influences.
                </p>
              </div>
              <div className="text-center space-y-4">
                <Users className="h-12 w-12 mx-auto text-primary" />
                <h3 className="text-xl font-semibold">Community Empowerment</h3>
                <p className="text-muted-foreground">
                  We believe in empowering citizens with the tools and information they need to actively participate in democracy.
                </p>
              </div>
            </div>
          </div>

          <div>
            <h2 className="text-2xl font-bold mb-8 text-center">Our Methodology</h2>
            <Card>
              <CardContent className="pt-6">
                <p className="text-muted-foreground">
                  Rank and File aggregates data from various official sources, including:
                </p>
                <ul className="list-disc list-inside mt-4 space-y-2 text-muted-foreground">
                  <li>Congressional voting records and bill sponsorships</li>
                  <li>Federal Election Commission (FEC) campaign finance data</li>
                  <li>Financial disclosure reports</li>
                  <li>Public statements and press releases</li>
                  <li>Committee assignments and attendance records</li>
                </ul>
                <p className="mt-4 text-muted-foreground">
                  Our team of data analysts and political researchers carefully verifies and cross-references all information to ensure accuracy and completeness.
                </p>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </div>
  );
}