import Image from "next/image";
import Link from "next/link";
import { Button } from "@/components/ui/button";
import { ChevronRight, Search, Scale, BarChart3, DollarSign, FileText, Building2, Users } from "lucide-react";

export default function Home() {
  return (
      <>
        {/* Hero Section */}
        <section className="relative h-screen flex items-center overflow-hidden">
          <div className="absolute inset-0 bg-gradient-to-b from-background to-muted/30"/>
          <div className="absolute inset-0 hero-pattern"/>
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 relative">
            <div className="text-center space-y-4 sm:space-y-8 max-w-4xl mx-auto">
              <div className="flex justify-center">
                <Scale className="h-12 w-12 sm:h-16 sm:w-16 text-primary mb-2 sm:mb-4"/>
              </div>
              <h1 className="text-3xl sm:text-4xl md:text-6xl lg:text-7xl font-bold tracking-tight px-2 sm:px-0">
                Democracy, <span className="text-primary">Decoded</span>
              </h1>
              <p className="text-base sm:text-xl text-muted-foreground max-w-2xl mx-auto px-4">
                Track U.S. politicians' legislative activities, sponsorships, donors, and financial disclosures with
                unprecedented clarity.
              </p>
              <div className="flex flex-col sm:flex-row justify-center gap-3 sm:gap-4 px-4">
                <Button size="lg" className="text-base sm:text-lg w-full sm:w-auto" asChild>
                  <Link href="/search">
                    Start Exploring <ChevronRight className="ml-2 h-5 w-5"/>
                  </Link>
                </Button>
                <Button size="lg" variant="outline" className="text-base sm:text-lg w-full sm:w-auto" asChild>
                  <Link href="/methodology">Our Methodology</Link>
                </Button>
              </div>
            </div>
          </div>
        </section>

        {/* Features Section */}
        <section className="py-12 sm:py-24 bg-muted/50">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="text-center mb-10 sm:mb-16">
              <h2 className="text-2xl sm:text-3xl font-bold mb-3 sm:mb-4">Comprehensive Political Intelligence</h2>
              <p className="text-base sm:text-lg text-muted-foreground max-w-2xl mx-auto">
                Access detailed insights into political activities, financial connections, and legislative patterns.
              </p>
            </div>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 sm:gap-8">
              <div
                  className="bg-background p-5 sm:p-8 rounded-xl shadow-sm border hover:border-primary/50 transition-colors">
                <Building2 className="h-10 w-10 sm:h-12 sm:w-12 text-primary mb-4 sm:mb-6"/>
                <h3 className="text-lg sm:text-xl font-semibold mb-2 sm:mb-3">Legislative Tracking</h3>
                <p className="text-sm sm:text-base text-muted-foreground">
                  Monitor bills, votes, and legislative patterns with real-time updates and historical context.
                </p>
              </div>

              <div
                  className="bg-background p-5 sm:p-8 rounded-xl shadow-sm border hover:border-primary/50 transition-colors">
                <DollarSign className="h-10 w-10 sm:h-12 sm:w-12 text-primary mb-4 sm:mb-6"/>
                <h3 className="text-lg sm:text-xl font-semibold mb-2 sm:mb-3">Financial Transparency</h3>
                <p className="text-sm sm:text-base text-muted-foreground">
                  Follow the money with comprehensive data on campaign contributions and stock trades.
                </p>
              </div>

              <div
                  className="bg-background p-5 sm:p-8 rounded-xl shadow-sm border hover:border-primary/50 transition-colors">
                <Users className="h-10 w-10 sm:h-12 sm:w-12 text-primary mb-4 sm:mb-6"/>
                <h3 className="text-lg sm:text-xl font-semibold mb-2 sm:mb-3">Donor Networks</h3>
                <p className="text-sm sm:text-base text-muted-foreground">
                  Uncover connections between politicians and their financial supporters.
                </p>
              </div>
            </div>
          </div>
        </section>

        {/* Stats Section */}
        <section className="py-12 sm:py-24 bg-background">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-8 sm:gap-12">
              <div className="text-center space-y-1 sm:space-y-2">
                <div className="text-3xl sm:text-4xl md:text-5xl font-bold text-primary">100%</div>
                <div className="text-sm sm:text-base md:text-lg text-muted-foreground">
                  Congress Members Tracked
                </div>
              </div>
              <div className="text-center space-y-1 sm:space-y-2">
                <div className="text-3xl sm:text-4xl md:text-5xl font-bold text-primary">50K+</div>
                <div className="text-sm sm:text-base md:text-lg text-muted-foreground">
                  Bills Analyzed
                </div>
              </div>
              <div className="text-center space-y-1 sm:space-y-2">
                <div className="text-3xl sm:text-4xl md:text-5xl font-bold text-primary">$2B+</div>
                <div className="text-sm sm:text-base md:text-lg text-muted-foreground">
                  Campaign Finances Tracked
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* CTA Section */}
        <section className="py-12 sm:py-24 bg-primary">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
            <div className="max-w-3xl mx-auto space-y-4 sm:space-y-6">
              <h2 className="text-2xl sm:text-3xl md:text-4xl font-bold text-primary-foreground">
                Ready to Explore Democracy?
              </h2>
              <p className="text-base sm:text-xl text-primary-foreground/90 mb-6 sm:mb-8">
                Join thousands of citizens making informed decisions about their political representatives.
              </p>
              <Button size="lg" variant="secondary" className="text-base sm:text-lg w-full sm:w-auto" asChild>
                <Link href="/search">
                  Start Your Research <ChevronRight className="ml-2 h-5 w-5"/>
                </Link>
              </Button>
            </div>
          </div>
        </section>
      </>
  );
}