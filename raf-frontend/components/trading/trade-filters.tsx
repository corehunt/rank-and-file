"use client";

import { Card, CardHeader, CardContent } from "@/components/ui/card";
import { Label } from "@/components/ui/label";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";
import { Separator } from "@/components/ui/separator";
import { Checkbox } from "@/components/ui/checkbox";

interface TradeFiltersProps {
  filters: {
    type: string;
    industries: string[];
    amountRanges: string[];
  };
  onFilterChange: (type: string, value: string | string[]) => void;
}

export default function TradeFilters({ filters, onFilterChange }: TradeFiltersProps) {
  return (
    <Card className="sticky top-4">
      <CardHeader>
        <h2 className="text-lg font-semibold">Filters</h2>
      </CardHeader>
      <CardContent className="space-y-6">
        <div className="space-y-4">
          <h3 className="font-medium">Transaction Type</h3>
          <RadioGroup 
            value={filters.type}
            onValueChange={(value) => onFilterChange("type", value)}
          >
            <div className="flex items-center space-x-2">
              <RadioGroupItem value="all" id="all" />
              <Label htmlFor="all">All Types</Label>
            </div>
            <div className="flex items-center space-x-2">
              <RadioGroupItem value="Purchase" id="purchase" />
              <Label htmlFor="purchase">Purchase</Label>
            </div>
            <div className="flex items-center space-x-2">
              <RadioGroupItem value="Sale" id="sale" />
              <Label htmlFor="sale">Sale</Label>
            </div>
          </RadioGroup>
        </div>

        <Separator />

        <div className="space-y-4">
          <h3 className="font-medium">Amount Range</h3>
          <div className="space-y-2">
            <div className="flex items-center space-x-2">
              <Checkbox 
                id="range1"
                checked={filters.amountRanges.includes("1k-15k")}
                onCheckedChange={(checked) => {
                  const newRanges = checked
                    ? [...filters.amountRanges, "1k-15k"]
                    : filters.amountRanges.filter(r => r !== "1k-15k");
                  onFilterChange("amountRanges", newRanges);
                }}
              />
              <Label htmlFor="range1">$1,000 - $15,000</Label>
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox 
                id="range2"
                checked={filters.amountRanges.includes("15k-50k")}
                onCheckedChange={(checked) => {
                  const newRanges = checked
                    ? [...filters.amountRanges, "15k-50k"]
                    : filters.amountRanges.filter(r => r !== "15k-50k");
                  onFilterChange("amountRanges", newRanges);
                }}
              />
              <Label htmlFor="range2">$15,000 - $50,000</Label>
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox 
                id="range3"
                checked={filters.amountRanges.includes("50k-100k")}
                onCheckedChange={(checked) => {
                  const newRanges = checked
                    ? [...filters.amountRanges, "50k-100k"]
                    : filters.amountRanges.filter(r => r !== "50k-100k");
                  onFilterChange("amountRanges", newRanges);
                }}
              />
              <Label htmlFor="range3">$50,000 - $100,000</Label>
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox 
                id="range4"
                checked={filters.amountRanges.includes("100k-plus")}
                onCheckedChange={(checked) => {
                  const newRanges = checked
                    ? [...filters.amountRanges, "100k-plus"]
                    : filters.amountRanges.filter(r => r !== "100k-plus");
                  onFilterChange("amountRanges", newRanges);
                }}
              />
              <Label htmlFor="range4">$100,000+</Label>
            </div>
          </div>
        </div>

        <Separator />

        <div className="space-y-4">
          <h3 className="font-medium">Industry</h3>
          <div className="space-y-2">
            <div className="flex items-center space-x-2">
              <Checkbox 
                id="tech"
                checked={filters.industries.includes("Technology")}
                onCheckedChange={(checked) => {
                  const newIndustries = checked
                    ? [...filters.industries, "Technology"]
                    : filters.industries.filter(i => i !== "Technology");
                  onFilterChange("industries", newIndustries);
                }}
              />
              <Label htmlFor="tech">Technology</Label>
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
          </div>
        </div>
      </CardContent>
    </Card>
  );
}