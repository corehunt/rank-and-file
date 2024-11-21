"use client";

import { Card, CardHeader, CardContent } from "@/components/ui/card";
import { Label } from "@/components/ui/label";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";
import { Separator } from "@/components/ui/separator";
import { Checkbox } from "@/components/ui/checkbox";

interface DonorFiltersProps {
  filters: {
    type: string;
    industries: string[];
    donationRanges: string[];
  };
  onFilterChange: (type: string, value: string | string[]) => void;
}

export default function DonorFilters({ filters, onFilterChange }: DonorFiltersProps) {
  return (
    <Card className="sticky top-4">
      <CardHeader>
        <h2 className="text-lg font-semibold">Filters</h2>
      </CardHeader>
      <CardContent className="space-y-6">
        <div className="space-y-4">
          <h3 className="font-medium">Donor Type</h3>
          <RadioGroup 
            value={filters.type}
            onValueChange={(value) => onFilterChange("type", value)}
          >
            <div className="flex items-center space-x-2">
              <RadioGroupItem value="all" id="all" />
              <Label htmlFor="all">All Types</Label>
            </div>
            <div className="flex items-center space-x-2">
              <RadioGroupItem value="Political Action Committee" id="pac" />
              <Label htmlFor="pac">PACs</Label>
            </div>
            <div className="flex items-center space-x-2">
              <RadioGroupItem value="Corporation" id="corporation" />
              <Label htmlFor="corporation">Corporations</Label>
            </div>
            <div className="flex items-center space-x-2">
              <RadioGroupItem value="Individual" id="individual" />
              <Label htmlFor="individual">Individuals</Label>
            </div>
          </RadioGroup>
        </div>

        <Separator />

        <div className="space-y-4">
          <h3 className="font-medium">Industry</h3>
          <div className="space-y-2">
            <div className="flex items-center space-x-2">
              <Checkbox 
                id="technology"
                checked={filters.industries.includes("Technology")}
                onCheckedChange={(checked) => {
                  const newIndustries = checked
                    ? [...filters.industries, "Technology"]
                    : filters.industries.filter(i => i !== "Technology");
                  onFilterChange("industries", newIndustries);
                }}
              />
              <Label htmlFor="technology">Technology</Label>
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox 
                id="energy"
                checked={filters.industries.includes("Energy")}
                onCheckedChange={(checked) => {
                  const newIndustries = checked
                    ? [...filters.industries, "Energy"]
                    : filters.industries.filter(i => i !== "Energy");
                  onFilterChange("industries", newIndustries);
                }}
              />
              <Label htmlFor="energy">Energy</Label>
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox 
                id="finance"
                checked={filters.industries.includes("Finance")}
                onCheckedChange={(checked) => {
                  const newIndustries = checked
                    ? [...filters.industries, "Finance"]
                    : filters.industries.filter(i => i !== "Finance");
                  onFilterChange("industries", newIndustries);
                }}
              />
              <Label htmlFor="finance">Finance</Label>
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox 
                id="healthcare"
                checked={filters.industries.includes("Healthcare")}
                onCheckedChange={(checked) => {
                  const newIndustries = checked
                    ? [...filters.industries, "Healthcare"]
                    : filters.industries.filter(i => i !== "Healthcare");
                  onFilterChange("industries", newIndustries);
                }}
              />
              <Label htmlFor="healthcare">Healthcare</Label>
            </div>
          </div>
        </div>

        <Separator />

        <div className="space-y-4">
          <h3 className="font-medium">Donation Amount</h3>
          <div className="space-y-2">
            <div className="flex items-center space-x-2">
              <Checkbox 
                id="under100k"
                checked={filters.donationRanges.includes("under100k")}
                onCheckedChange={(checked) => {
                  const newRanges = checked
                    ? [...filters.donationRanges, "under100k"]
                    : filters.donationRanges.filter(r => r !== "under100k");
                  onFilterChange("donationRanges", newRanges);
                }}
              />
              <Label htmlFor="under100k">Under $100,000</Label>
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox 
                id="100k-500k"
                checked={filters.donationRanges.includes("100k-500k")}
                onCheckedChange={(checked) => {
                  const newRanges = checked
                    ? [...filters.donationRanges, "100k-500k"]
                    : filters.donationRanges.filter(r => r !== "100k-500k");
                  onFilterChange("donationRanges", newRanges);
                }}
              />
              <Label htmlFor="100k-500k">$100,000 - $500,000</Label>
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox 
                id="over500k"
                checked={filters.donationRanges.includes("over500k")}
                onCheckedChange={(checked) => {
                  const newRanges = checked
                    ? [...filters.donationRanges, "over500k"]
                    : filters.donationRanges.filter(r => r !== "over500k");
                  onFilterChange("donationRanges", newRanges);
                }}
              />
              <Label htmlFor="over500k">Over $500,000</Label>
            </div>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}